package com.fonepay.devportal.modules.notification.webhook.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.enums.BroadcastPriority;
import com.fonepay.devportal.modules.notification.webhook.BroadcastWebhookClient;
import com.fonepay.devportal.modules.notification.webhook.BroadcastWebhookProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastWebhookClientImpl implements BroadcastWebhookClient {

    private final BroadcastWebhookProperties properties;
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public void sendBroadcastWebhook(Broadcast broadcast) {
        if (!properties.isEnabled()) {
            return;
        }

        String webhookUrl = properties.getUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Broadcast webhook is enabled but no URL is configured.");
            return;
        }

        if (!meetsPriorityThreshold(broadcast.getPriority(), properties.getMinPriority())) {
            log.debug("Broadcast [{}] priority [{}] below webhook threshold [{}]",
                    broadcast.getId(), broadcast.getPriority(), properties.getMinPriority());
            return;
        }

        try {
            Map<String, Object> payload = buildWebhookPayload(broadcast);

            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully sent broadcast [{}] to external webhook [{}]", broadcast.getId(), webhookUrl);
        } catch (Exception e) {
            log.warn("Failed to dispatch broadcast [{}] to webhook [{}]: {}", broadcast.getId(), webhookUrl, e.getMessage());
        }
    }

    private boolean meetsPriorityThreshold(BroadcastPriority actual, BroadcastPriority threshold) {
        if (actual == null) {
            actual = BroadcastPriority.NORMAL;
        }
        if (threshold == null) {
            threshold = BroadcastPriority.HIGH;
        }
        return actual.ordinal() >= threshold.ordinal();
    }

    private Map<String, Object> buildWebhookPayload(Broadcast broadcast) {
        String priorityEmoji = switch (broadcast.getPriority() != null ? broadcast.getPriority() : BroadcastPriority.NORMAL) {
            case URGENT -> "🚨 *[URGENT]*";
            case HIGH -> "⚠️ *[HIGH]*";
            case NORMAL -> "📢 *[INFO]*";
            case LOW -> "ℹ️ *[LOW]*";
        };

        String text = String.format("%s *%s*\n%s\nTarget: `%s` | Category: `%s`",
                priorityEmoji,
                broadcast.getTitle(),
                broadcast.getMessage(),
                broadcast.getTargetRole(),
                broadcast.getCategory());

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);

        // Slack & Teams block kit compatibility
        Map<String, Object> section = Map.of(
                "type", "section",
                "text", Map.of(
                        "type", "mrkdwn",
                        "text", text));
        payload.put("blocks", List.of(section));

        return payload;
    }
}
