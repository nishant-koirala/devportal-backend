package com.fonepay.devportal.modules.cms.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.PageVersion;
import com.fonepay.devportal.modules.cms.dto.request.PublishPageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageVersionResponse;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.fonepay.devportal.modules.cms.mapper.PageMapper;
import com.fonepay.devportal.modules.cms.mapper.PageVersionMapper;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.repository.PageVersionRepository;
import com.fonepay.devportal.modules.cms.service.AuditLogService;
import com.fonepay.devportal.modules.cms.service.PublishService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishServiceImpl implements PublishService {

    private final PageRepository pageRepository;
    private final PageVersionRepository pageVersionRepository;
    private final AuditLogService auditLogService;
    private final PageMapper pageMapper;
    private final PageVersionMapper pageVersionMapper;
    private final Clock clock;

    @Override
    public PageMetaResponse publishPage(String pageId, PublishPageRequest request, String adminId, String sourceIp) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + pageId));
        return publishPage(page, request, adminId, sourceIp);
    }

    @Override
    public PageMetaResponse publishPage(Page page, PublishPageRequest request, String adminId, String sourceIp) {
        if (page == null || page.getId() == null) {
            throw new ResourceNotFoundException("Page not found");
        }

        String pageId = page.getId();
        log.info("Publishing page {} by admin {}", pageId, adminId);

        if (page.getStatus() == PageStatus.ARCHIVED) {
            throw new BadRequestException("Cannot publish an archived page");
        }
        if (page.getStatus() != PageStatus.IN_REVIEW) {
            throw new BadRequestException(
                    "Only pages with IN_REVIEW status can be published. Current status: " + page.getStatus());
        }

        // F20-49: Enforce cURL code sample validation for ENDPOINT pages
        if (com.fonepay.devportal.modules.cms.enums.PageType.ENDPOINT.equals(page.getType())) {
            boolean hasValidCurl = false;
            if (page.getDraftBlocks() != null) {
                for (Block block : page.getDraftBlocks()) {
                    if (block.getData() instanceof com.fonepay.devportal.modules.cms.document.EndpointBlockData) {
                        com.fonepay.devportal.modules.cms.document.EndpointBlockData endpointData = 
                            (com.fonepay.devportal.modules.cms.document.EndpointBlockData) block.getData();
                        if (endpointData.getCurlSample() != null && !endpointData.getCurlSample().trim().isEmpty()) {
                            hasValidCurl = true;
                            break;
                        }
                    }
                }
            }
            if (!hasValidCurl) {
                throw new BadRequestException("Cannot publish an ENDPOINT page without a valid cURL sample in its Endpoint Block.");
            }
        }

        List<Block> draftBlocks = page.getDraftBlocks() != null ? page.getDraftBlocks() : Collections.emptyList();
        List<Block> snapshotBlocks = new ArrayList<>(draftBlocks);
        page.setPublishedBlocks(snapshotBlocks);

        int nextVersion = pageVersionRepository.findTopByPageIdOrderByVersionNumberDesc(pageId)
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        Instant now = Instant.now(clock);

        PageVersion pageVersion = PageVersion.builder()
                .id(IdGenerator.nextUlid())
                .pageId(page.getId())
                .versionNumber(nextVersion)
                .publishedBlocks(new ArrayList<>(snapshotBlocks))
                .publishedAt(now)
                .publishedBy(adminId)
                .commitMessage(request != null && request.getCommitMessage() != null ? request.getCommitMessage().trim()
                        : "Published version " + nextVersion)
                .build();

        pageVersionRepository.save(pageVersion);
        log.info("Created page version {} for page {}", nextVersion, pageId);

        page.setStatus(PageStatus.PUBLISHED);
        page.setLastPublishedAt(now);
        page.setUpdatedAt(now);
        Page savedPage = pageRepository.save(page);
        log.info("Updated page {} status to PUBLISHED", pageId);

        auditLogService.logAction(adminId, "PAGE_PUBLISH", savedPage.getId(), "PAGE", sourceIp);

        return pageMapper.toMetaResponse(savedPage);
    }

    @Override
    public List<PageMetaResponse> publishPagesForProduct(String productId, String adminId, String sourceIp,
            String commitMessage) {
        if (productId == null || productId.isBlank()) {
            return Collections.emptyList();
        }

        List<Page> pages = pageRepository.findByProductIdAndStatusNotOrderByPageOrderAsc(productId.trim(),
                PageStatus.ARCHIVED);
        if (pages.isEmpty()) {
            log.info("No active pages found to publish for product ID: {}", productId);
            return Collections.emptyList();
        }

        Instant now = Instant.now(clock);
        List<PageMetaResponse> publishedPages = new ArrayList<>();

        for (Page page : pages) {
            // F20-49: Enforce cURL code sample validation for ENDPOINT pages
            if (com.fonepay.devportal.modules.cms.enums.PageType.ENDPOINT.equals(page.getType())) {
                boolean hasValidCurl = false;
                if (page.getDraftBlocks() != null) {
                    for (Block block : page.getDraftBlocks()) {
                        if (block.getData() instanceof com.fonepay.devportal.modules.cms.document.EndpointBlockData) {
                            com.fonepay.devportal.modules.cms.document.EndpointBlockData endpointData = 
                                (com.fonepay.devportal.modules.cms.document.EndpointBlockData) block.getData();
                            if (endpointData.getCurlSample() != null && !endpointData.getCurlSample().trim().isEmpty()) {
                                hasValidCurl = true;
                                break;
                            }
                        }
                    }
                }
                if (!hasValidCurl) {
                    throw new BadRequestException("Cannot publish an ENDPOINT page (ID: " + page.getId() + ") without a valid cURL sample in its Endpoint Block.");
                }
            }

            List<Block> draftBlocks = page.getDraftBlocks() != null ? page.getDraftBlocks() : Collections.emptyList();
            List<Block> snapshotBlocks = new ArrayList<>(draftBlocks);
            page.setPublishedBlocks(snapshotBlocks);

            int nextVersion = pageVersionRepository.findTopByPageIdOrderByVersionNumberDesc(page.getId())
                    .map(v -> v.getVersionNumber() + 1)
                    .orElse(1);

            PageVersion pageVersion = PageVersion.builder()
                    .id(IdGenerator.nextUlid())
                    .pageId(page.getId())
                    .versionNumber(nextVersion)
                    .publishedBlocks(new ArrayList<>(snapshotBlocks))
                    .publishedAt(now)
                    .publishedBy(adminId)
                    .commitMessage(commitMessage != null && !commitMessage.isBlank()
                            ? commitMessage.trim()
                            : "Auto-published upon product approval (version " + nextVersion + ")")
                    .build();

            pageVersionRepository.save(pageVersion);
            log.info("Created page version {} for page {} during product approval", nextVersion, page.getId());

            page.setStatus(PageStatus.PUBLISHED);
            page.setLastPublishedAt(now);
            page.setUpdatedAt(now);
            Page savedPage = pageRepository.save(page);

            auditLogService.logAction(adminId, "PAGE_PUBLISH", savedPage.getId(), "PAGE", sourceIp);
            publishedPages.add(pageMapper.toMetaResponse(savedPage));
        }

        log.info("Auto-published {} page(s) for product ID: {}", publishedPages.size(), productId);
        return publishedPages;
    }

    @Override
    public List<PageVersionResponse> getPageVersions(String pageId) {
        requirePageExists(pageId);
        List<PageVersion> versions = pageVersionRepository.findByPageIdOrderByVersionNumberDesc(pageId);
        return pageVersionMapper.toResponseList(versions);
    }

    @Override
    public PageVersionResponse getPageVersion(String pageId, int versionNumber) {
        requirePageExists(pageId);
        PageVersion version = pageVersionRepository.findByPageIdAndVersionNumber(pageId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Page version " + versionNumber + " not found for page ID: " + pageId));
        return pageVersionMapper.toResponse(version);
    }

    @Override
    public PageMetaResponse revertToVersion(String pageId, int versionNumber, String adminId, String sourceIp) {
        log.info("Reverting page {} to version {} by admin {}", pageId, versionNumber, adminId);

        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found with ID: " + pageId));

        if (page.getStatus() == PageStatus.ARCHIVED) {
            throw new BadRequestException("Cannot revert an archived page");
        }
        if (page.getStatus() == PageStatus.IN_REVIEW) {
            throw new BadRequestException(
                    "Page is IN_REVIEW and cannot be edited until an admin approves or rejects it");
        }

        PageVersion targetVersion = pageVersionRepository.findByPageIdAndVersionNumber(pageId, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Page version " + versionNumber + " not found for page ID: " + pageId));

        // Copy snapshot blocks from version into draftBlocks
        List<Block> versionBlocks = targetVersion.getPublishedBlocks() != null
                ? targetVersion.getPublishedBlocks()
                : Collections.emptyList();
        page.setDraftBlocks(new ArrayList<>(versionBlocks));
        page.setUpdatedAt(Instant.now(clock));

        Page savedPage = pageRepository.save(page);

        auditLogService.logAction(adminId, "REVERT_PAGE_VERSION", savedPage.getId(), "PAGE", sourceIp);
        log.info("Page {} reverted to version {} in draft mode", pageId, versionNumber);

        return pageMapper.toMetaResponse(savedPage);
    }

    private void requirePageExists(String pageId) {
        if (pageId == null || pageId.isBlank() || !pageRepository.existsById(pageId)) {
            throw new ResourceNotFoundException("Page not found with ID: " + pageId);
        }
    }
}
