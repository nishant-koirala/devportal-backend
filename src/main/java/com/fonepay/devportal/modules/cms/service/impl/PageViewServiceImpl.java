package com.fonepay.devportal.modules.cms.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.util.IdGenerator;
import com.fonepay.devportal.modules.cms.document.PageView;
import com.fonepay.devportal.modules.cms.repository.PageViewRepository;
import com.fonepay.devportal.modules.cms.service.PageViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageViewServiceImpl implements PageViewService {

    private final PageViewRepository pageViewRepository;
    private final Clock clock;

    @Override
    public void recordView(String developerId, String pageId) {
        if (developerId == null) {
            return; // Only tracking authenticated developers
        }

        Instant now = clock.instant();
        Optional<PageView> lastView = pageViewRepository.findTopByPageIdAndDeveloperIdOrderByViewedAtDesc(pageId, developerId);

        if (lastView.isPresent()) {
            long secondsSinceLastView = ChronoUnit.SECONDS.between(lastView.get().getViewedAt(), now);
            if (secondsSinceLastView < 60) {
                log.debug("Skipping page view for developer {} on page {} (deduplication window)", developerId, pageId);
                return;
            }
        }

        PageView pageView = PageView.builder()
                .id(IdGenerator.nextUlid())
                .pageId(pageId)
                .developerId(developerId)
                .viewedAt(now)
                .build();
        
        pageViewRepository.save(pageView);
        log.info("Recorded page view for developer {} on page {}", developerId, pageId);
    }
}
