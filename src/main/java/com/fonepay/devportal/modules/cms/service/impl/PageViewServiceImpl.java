package com.fonepay.devportal.modules.cms.service.impl;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.cms.service.PageViewService;

@Service
public class PageViewServiceImpl implements PageViewService {

    @Override
    public void recordView(String developerId, String pageId) {
        // TODO: Implement 60s sliding window deduplication in [F20-50]
    }
}
