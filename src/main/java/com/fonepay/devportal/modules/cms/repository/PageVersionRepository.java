package com.fonepay.devportal.modules.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.cms.document.PageVersion;

@Repository
public interface PageVersionRepository extends MongoRepository<PageVersion, String> {

    Optional<PageVersion> findTopByPageIdOrderByVersionNumberDesc(String pageId);

    List<PageVersion> findByPageIdOrderByVersionNumberDesc(String pageId);

    Optional<PageVersion> findByPageIdAndVersionNumber(String pageId, int versionNumber);

    boolean existsByPageId(String pageId);

    void deleteByPageId(String pageId);
}
