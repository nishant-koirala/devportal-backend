package com.fonepay.devportal.modules.notification.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.modules.notification.dto.request.CreateBroadcastRequest;
import com.fonepay.devportal.modules.notification.enums.BroadcastCategory;
import com.fonepay.devportal.modules.notification.enums.BroadcastDisplayMode;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.service.BroadcastAdminService;
import com.fonepay.devportal.modules.notification.service.SystemBroadcastService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemBroadcastServiceImpl implements SystemBroadcastService {

    private static final String SYSTEM_ADMIN_ID = "SYSTEM";

    private final BroadcastAdminService broadcastAdminService;

    @Override
    public void notifyPageReviewSubmitted(String pageId, String pageTitle, String productSlug, String submittedByUserId) {
        log.info("System broadcast: Review submitted for page [{}] ({})", pageId, pageTitle);

        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Review Request: " + (pageTitle != null ? pageTitle : pageId))
                .message(String.format("Page '%s' (%s) was submitted for review by user [%s].",
                        pageTitle != null ? pageTitle : pageId, productSlug != null ? productSlug : "", submittedByUserId))
                .targetRole(BroadcastTargetRole.ADMINS_ONLY)
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL, BroadcastDisplayMode.POPUP_TOAST))
                .priority(BroadcastPriority.HIGH)
                .category(BroadcastCategory.WORKFLOW)
                .actionUrl("/cms/pages/" + pageId)
                .actionLabel("Review Page")
                .isDismissible(true)
                .build();

        broadcastAdminService.createBroadcast(request, SYSTEM_ADMIN_ID);
    }

    @Override
    public void notifyPageApproved(String pageId, String pageTitle, String productSlug, String approvedByAdminId) {
        log.info("System broadcast: Page [{}] ({}) approved", pageId, pageTitle);

        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Page Approved: " + (pageTitle != null ? pageTitle : pageId))
                .message(String.format("Page '%s' (%s) has been approved by admin [%s] and published.",
                        pageTitle != null ? pageTitle : pageId, productSlug != null ? productSlug : "", approvedByAdminId))
                .targetRole(BroadcastTargetRole.CMS_EDITORS)
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL))
                .priority(BroadcastPriority.NORMAL)
                .category(BroadcastCategory.WORKFLOW)
                .actionUrl("/cms/pages/" + pageId)
                .actionLabel("View Page")
                .isDismissible(true)
                .build();

        broadcastAdminService.createBroadcast(request, SYSTEM_ADMIN_ID);
    }

    @Override
    public void notifyPageRejected(String pageId, String pageTitle, String productSlug, String reason, String rejectedByAdminId) {
        log.info("System broadcast: Page [{}] ({}) rejected", pageId, pageTitle);

        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Page Returned: " + (pageTitle != null ? pageTitle : pageId))
                .message(String.format("Page '%s' (%s) was returned to draft by admin [%s]. Feedback: %s",
                        pageTitle != null ? pageTitle : pageId, productSlug != null ? productSlug : "", rejectedByAdminId, reason != null ? reason : "No reason specified"))
                .targetRole(BroadcastTargetRole.CMS_EDITORS)
                .displayModes(Set.of(BroadcastDisplayMode.NOTIFICATION_BELL, BroadcastDisplayMode.POPUP_TOAST))
                .priority(BroadcastPriority.HIGH)
                .category(BroadcastCategory.WORKFLOW)
                .actionUrl("/cms/pages/" + pageId)
                .actionLabel("Edit Draft")
                .isDismissible(true)
                .build();

        broadcastAdminService.createBroadcast(request, SYSTEM_ADMIN_ID);
    }

    @Override
    public void notifyProductStatusChanged(String productId, String productName, String oldStatus, String newStatus, String adminId) {
        log.info("System broadcast: Product [{}] status changed from {} to {}", productId, oldStatus, newStatus);

        boolean isHighImpact = "DEPRECATED".equalsIgnoreCase(newStatus)
                || "MAINTENANCE".equalsIgnoreCase(newStatus)
                || "INACTIVE".equalsIgnoreCase(newStatus);

        BroadcastPriority priority = isHighImpact ? BroadcastPriority.HIGH : BroadcastPriority.NORMAL;

        CreateBroadcastRequest request = CreateBroadcastRequest.builder()
                .title("Product Status Update: " + (productName != null ? productName : productId))
                .message(String.format("Product '%s' status changed from %s to %s by admin [%s].",
                        productName != null ? productName : productId, oldStatus, newStatus, adminId))
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .displayModes(Set.of(BroadcastDisplayMode.GLOBAL_BANNER, BroadcastDisplayMode.NOTIFICATION_BELL))
                .priority(priority)
                .category(BroadcastCategory.ANNOUNCEMENT)
                .actionUrl("/cms/products/" + productId)
                .actionLabel("View Product")
                .isDismissible(true)
                .build();

        broadcastAdminService.createBroadcast(request, SYSTEM_ADMIN_ID);
    }
}
