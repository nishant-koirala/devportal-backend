package com.fonepay.devportal.modules.notification.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;
import com.fonepay.devportal.modules.notification.webhook.impl.BroadcastWebhookClientImpl;

@ExtendWith(MockitoExtension.class)
class BroadcastWebhookClientTest {

    private BroadcastWebhookProperties properties;
    private BroadcastWebhookClient webhookClient;

    @BeforeEach
    void setUp() {
        properties = new BroadcastWebhookProperties();
        webhookClient = new BroadcastWebhookClientImpl(properties);
    }

    @Test
    void sendBroadcastWebhook_WhenDisabled_ShouldNotThrowException() {
        properties.setEnabled(false);

        Broadcast broadcast = Broadcast.builder()
                .id("bc_1")
                .title("Test Alert")
                .priority(BroadcastPriority.URGENT)
                .build();

        // Should return early and safely without exceptions
        webhookClient.sendBroadcastWebhook(broadcast);
    }

    @Test
    void sendBroadcastWebhook_WhenUrlMissing_ShouldNotThrowException() {
        properties.setEnabled(true);
        properties.setUrl(null);

        Broadcast broadcast = Broadcast.builder()
                .id("bc_1")
                .title("Test Alert")
                .priority(BroadcastPriority.URGENT)
                .build();

        webhookClient.sendBroadcastWebhook(broadcast);
    }

    @Test
    void sendBroadcastWebhook_WhenPriorityBelowThreshold_ShouldSkip() {
        properties.setEnabled(true);
        properties.setUrl("https://hooks.slack.com/services/mock/webhook");
        properties.setMinPriority(BroadcastPriority.HIGH);

        Broadcast lowPriorityBroadcast = Broadcast.builder()
                .id("bc_low")
                .title("Low Priority Info")
                .priority(BroadcastPriority.LOW)
                .targetRole(BroadcastTargetRole.ALL_STAFF)
                .build();

        // Should skip webhook call safely
        webhookClient.sendBroadcastWebhook(lowPriorityBroadcast);
    }
}
