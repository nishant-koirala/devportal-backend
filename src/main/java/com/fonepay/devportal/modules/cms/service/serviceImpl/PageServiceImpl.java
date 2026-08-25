package com.fonepay.devportal.modules.cms.service.serviceImpl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.DuplicateResourceException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.dto.request.CreatePageRequest;
import com.fonepay.devportal.modules.cms.dto.request.PageHierarchyUpdateDto;
import com.fonepay.devportal.modules.cms.dto.request.UpdatePageRequest;
import com.fonepay.devportal.modules.cms.dto.response.PageMetaResponse;
import com.fonepay.devportal.modules.cms.dto.response.PageTreeNodeResponse;
import com.fonepay.devportal.modules.cms.enums.PageStatus;
import com.fonepay.devportal.modules.cms.mapper.PageMapper;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.service.PageService;
import com.fonepay.devportal.modules.cms.service.PageTreeBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private static final int MAX_TREE_DEPTH = 3;

    private final PageRepository pageRepository;
    private final MongoTemplate mongoTemplate;
    private final PageMapper pageMapper;
    private final Clock clock;

    @Override
    public PageMetaResponse createPage(String productId, CreatePageRequest request, String createdBy) {
        ensureProductExists(productId);

        String parentId = normalizeParentId(request.getParentId());
        if (parentId != null) {
            Page parent = requirePage(parentId);
            if (!productId.equals(parent.getProductId())) {
                throw new BadRequestException("Parent page does not belong to the specified product");
            }
            if (parent.getStatus() == PageStatus.ARCHIVED) {
                throw new BadRequestException("Cannot create a page under an archived parent");
            }
            assertDepthAllowed(depthOf(parent, loadPagesById(productId)) + 1);
        }

        if (pageRepository.existsByProductIdAndSlug(productId, request.getSlug())) {
            throw new DuplicateResourceException(
                    "A page with slug '" + request.getSlug() + "' already exists for this product");
        }

        Instant now = clock.instant();
        Page page = new Page();
        page.setId(IdGenerator.nextUlid());
        page.setProductId(productId);
        page.setParentId(parentId);
        page.setPageOrder(nextPageOrder(productId, parentId));
        page.setTitle(request.getTitle().trim());
        page.setSlug(request.getSlug());
        page.setStatus(PageStatus.DRAFT);
        page.setCreatedAt(now);
        page.setUpdatedAt(now);
        page.setCreatedBy(createdBy);

        try {
            Page saved = pageRepository.save(page);
            log.info("Created page {} under product {}", saved.getId(), productId);
            return pageMapper.toMetaResponse(saved);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException(
                    "A page with slug '" + request.getSlug() + "' already exists for this product");
        }
    }

    @Override
    public PageMetaResponse getPage(String pageId) {
        return pageMapper.toMetaResponse(requirePage(pageId));
    }

    @Override
    public PageMetaResponse updatePage(String pageId, UpdatePageRequest request) {
        if (isBlank(request.getTitle()) && isBlank(request.getSlug())) {
            throw new BadRequestException("At least one of title or slug must be provided");
        }

        Page page = requirePage(pageId);
        if (page.getStatus() == PageStatus.ARCHIVED) {
            throw new BadRequestException("Cannot update an archived page");
        }

        if (!isBlank(request.getTitle())) {
            page.setTitle(request.getTitle().trim());
        }

        if (!isBlank(request.getSlug()) && !request.getSlug().equals(page.getSlug())) {
            if (pageRepository.existsByProductIdAndSlugAndIdNot(page.getProductId(), request.getSlug(), pageId)) {
                throw new DuplicateResourceException(
                        "A page with slug '" + request.getSlug() + "' already exists for this product");
            }
            page.setSlug(request.getSlug());
        }

        page.setUpdatedAt(clock.instant());

        try {
            Page saved = pageRepository.save(page);
            log.info("Updated page {}", pageId);
            return pageMapper.toMetaResponse(saved);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateResourceException(
                    "A page with slug '" + request.getSlug() + "' already exists for this product");
        }
    }

    @Override
    public PageMetaResponse archivePage(String pageId) {
        Page page = requirePage(pageId);
        if (page.getStatus() == PageStatus.ARCHIVED) {
            throw new BadRequestException("Page is already archived");
        }

        Map<String, Page> byId = loadPagesById(page.getProductId());
        List<String> toArchive = collectDescendantIds(pageId, byId);
        toArchive.add(0, pageId);

        Instant now = clock.instant();
        pageRepository.bulkSetStatus(toArchive, PageStatus.ARCHIVED, now);

        page.setStatus(PageStatus.ARCHIVED);
        page.setUpdatedAt(now);

        log.info("Archived page {} and {} descendant(s)", pageId, toArchive.size() - 1);
        return pageMapper.toMetaResponse(page);
    }

    @Override
    public List<PageTreeNodeResponse> getPageTree(String productId) {
        ensureProductExists(productId);
        List<Page> pages = pageRepository.findByProductIdAndStatusNotOrderByPageOrderAsc(
                productId, PageStatus.ARCHIVED);
        return PageTreeBuilder.build(pages);
    }

    @Override
    public void movePages(String productId, List<PageHierarchyUpdateDto> updates) {
        ensureProductExists(productId);
        if (updates == null || updates.isEmpty()) {
            throw new BadRequestException("At least one hierarchy update is required");
        }

        Set<String> seenIds = new HashSet<>();
        for (PageHierarchyUpdateDto update : updates) {
            if (!seenIds.add(update.getPageId())) {
                throw new BadRequestException("Duplicate page ID in reorder request: " + update.getPageId());
            }
        }

        Map<String, Page> byId = loadPagesById(productId);
        Map<String, String> proposedParent = new HashMap<>();
        for (Page page : byId.values()) {
            proposedParent.put(page.getId(), page.getParentId());
        }

        for (PageHierarchyUpdateDto update : updates) {
            Page page = byId.get(update.getPageId());
            if (page == null) {
                throw new ResourceNotFoundException("Page not found in this product: " + update.getPageId());
            }
            if (page.getStatus() == PageStatus.ARCHIVED) {
                throw new BadRequestException("Cannot move archived page: " + update.getPageId());
            }

            String newParentId = normalizeParentId(update.getParentId());
            if (update.getPageId().equals(newParentId)) {
                throw new BadRequestException("A page cannot be its own parent");
            }
            if (newParentId != null) {
                Page parent = byId.get(newParentId);
                if (parent == null) {
                    throw new BadRequestException("Parent page does not belong to this product: " + newParentId);
                }
                if (parent.getStatus() == PageStatus.ARCHIVED) {
                    throw new BadRequestException("Cannot move a page under an archived parent");
                }
            }
            proposedParent.put(update.getPageId(), newParentId);
        }

        assertAcyclicAndWithinDepth(proposedParent);

        pageRepository.bulkUpdateHierarchy(updates, clock.instant());
        log.info("Reordered {} page(s) for product {}", updates.size(), productId);
    }

    private void ensureProductExists(String productId) {
        boolean exists = mongoTemplate.exists(
                Query.query(Criteria.where("_id").is(productId)),
                Product.class);
        if (!exists) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
    }

    private Page requirePage(String pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + pageId));
    }

    private int nextPageOrder(String productId, String parentId) {
        List<Page> siblings = parentId == null
                ? pageRepository.findByProductIdAndParentIdIsNullOrderByPageOrderAsc(productId)
                : pageRepository.findByProductIdAndParentIdOrderByPageOrderAsc(productId, parentId);
        return siblings.stream().mapToInt(Page::getPageOrder).max().orElse(-1) + 1;
    }

    private Map<String, Page> loadPagesById(String productId) {
        Map<String, Page> byId = new HashMap<>();
        for (Page page : pageRepository.findByProductIdOrderByPageOrderAsc(productId)) {
            byId.put(page.getId(), page);
        }
        return byId;
    }

    private List<String> collectDescendantIds(String rootId, Map<String, Page> byId) {
        Map<String, List<String>> childrenByParent = new HashMap<>();
        for (Page page : byId.values()) {
            String parentId = page.getParentId();
            if (parentId != null && !parentId.isBlank()) {
                childrenByParent.computeIfAbsent(parentId, key -> new ArrayList<>()).add(page.getId());
            }
        }

        List<String> descendants = new ArrayList<>();
        Set<String> visiting = new HashSet<>();
        collectDescendants(rootId, childrenByParent, descendants, visiting);
        return descendants;
    }

    private void collectDescendants(
            String parentId,
            Map<String, List<String>> childrenByParent,
            List<String> descendants,
            Set<String> visiting) {
        if (!visiting.add(parentId)) {
            return;
        }
        for (String childId : childrenByParent.getOrDefault(parentId, List.of())) {
            descendants.add(childId);
            collectDescendants(childId, childrenByParent, descendants, visiting);
        }
        visiting.remove(parentId);
    }

    private int depthOf(Page page, Map<String, Page> byId) {
        int depth = 1;
        String currentParentId = page.getParentId();
        Set<String> seen = new HashSet<>();
        while (currentParentId != null && !currentParentId.isBlank()) {
            if (!seen.add(currentParentId)) {
                throw new BadRequestException("Page hierarchy contains a cycle");
            }
            depth++;
            Page parent = byId.get(currentParentId);
            if (parent == null) {
                break;
            }
            currentParentId = parent.getParentId();
        }
        return depth;
    }

    private void assertAcyclicAndWithinDepth(Map<String, String> parentById) {
        for (String pageId : parentById.keySet()) {
            int depth = 0;
            String current = pageId;
            Set<String> seen = new HashSet<>();
            while (current != null && !current.isBlank()) {
                if (!seen.add(current)) {
                    throw new BadRequestException("Cannot move a page under one of its descendants");
                }
                depth++;
                assertDepthAllowed(depth);
                current = parentById.get(current);
            }
        }
    }

    private void assertDepthAllowed(int depth) {
        if (depth > MAX_TREE_DEPTH) {
            throw new BadRequestException("Page hierarchy exceeds maximum depth of " + MAX_TREE_DEPTH);
        }
    }

    private static String normalizeParentId(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return parentId;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
