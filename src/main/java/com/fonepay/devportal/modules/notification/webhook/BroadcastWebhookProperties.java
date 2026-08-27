package com.fonepay.devportal.modules.notification.webhook;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.broadcast.webhook")
public class BroadcastWebhookProperties {

    /**
     * Whether outbound external webhook notification is enabled.
     */
    private boolean enabled = false;

    /**
     * External incoming webhook URL (Slack, MS Teams, Mattermost, Discord, etc.).
     */
    private String url;

    /**
     * Minimum broadcast priority required to trigger an outbound webhook (default: HIGH).
     */
    private BroadcastPriority minPriority = BroadcastPriority.HIGH;
}
