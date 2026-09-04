package com.fonepay.devportal.modules.cms.service;

public interface RedirectService {
    void createRedirect(String productId, String pageId, String oldPath, String newPath);
    String resolveRedirect(String path);
}
