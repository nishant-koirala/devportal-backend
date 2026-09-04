package com.fonepay.devportal.modules.cms.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.fonepay.devportal.modules.cms.document.PageView;

import java.util.Optional;

public interface PageViewRepository extends MongoRepository<PageView, String> {
    Optional<PageView> findTopByPageIdAndDeveloperIdOrderByViewedAtDesc(String pageId, String developerId);
}
