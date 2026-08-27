package com.fonepay.devportal.modules.notification.service;

public interface SystemBroadcastService {

    void notifyPageReviewSubmitted(String pageId, String pageTitle, String productSlug, String submittedByUserId);

    void notifyPageApproved(String pageId, String pageTitle, String productSlug, String approvedByAdminId);

    void notifyPageRejected(String pageId, String pageTitle, String productSlug, String reason, String rejectedByAdminId);

    void notifyProductStatusChanged(String productId, String productName, String oldStatus, String newStatus, String adminId);
}
