package com.fonepay.devportal.modules.cms.service.impl;

import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.Block;
import com.fonepay.devportal.modules.cms.document.Page;
import com.fonepay.devportal.modules.cms.document.Revision;
import com.fonepay.devportal.modules.cms.repository.PageRepository;
import com.fonepay.devportal.modules.cms.repository.RevisionRepository;
import com.fonepay.devportal.modules.cms.service.RevisionService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Clock;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevisionServiceImpl implements RevisionService {

    private final RevisionRepository revisionRepository;
    private final PageRepository pageRepository;
    private final Clock clock;

    @Override
    public Revision createSnapshot(String pageId, List<Block> blocks, String commitMessage, String createdBy) {
        Revision lastRevision = revisionRepository.findTopByPageIdOrderByVersionDesc(pageId);
        int nextVersion = (lastRevision != null) ? lastRevision.getVersion() + 1 : 1;

        Revision revision = Revision.builder()
                .id(IdGenerator.nextUlid())
                .pageId(pageId)
                .version(nextVersion)
                .blocksSnapshot(blocks)
                .commitMessage(commitMessage)
                .createdBy(createdBy)
                .createdAt(clock.instant())
                .build();

        Revision saved = revisionRepository.save(revision);
        log.info("Created revision v{} for page {}", nextVersion, pageId);
        return saved;
    }

    @Override
    public List<Revision> getRevisions(String pageId) {
        return revisionRepository.findByPageIdOrderByVersionDesc(pageId);
    }

    @Override
    public Revision getRevision(String pageId, int version) {
        return revisionRepository.findByPageIdAndVersion(pageId, version)
                .orElseThrow(() -> new ResourceNotFoundException("Revision v" + version + " not found for page " + pageId));
    }

    @Override
    public Revision revertToVersion(String pageId, int version, String revertedBy) {
        Revision revision = getRevision(pageId, version);
        
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + pageId));
        
        page.setDraftBlocks(revision.getBlocksSnapshot());
        page.setUpdatedAt(clock.instant());
        pageRepository.save(page);
        
        log.info("Page {} reverted to revision v{} by {}", pageId, version, revertedBy);
        
        // Create a new snapshot representing the revert
        return createSnapshot(pageId, page.getDraftBlocks(), "Reverted to v" + version, revertedBy);
    }
}
