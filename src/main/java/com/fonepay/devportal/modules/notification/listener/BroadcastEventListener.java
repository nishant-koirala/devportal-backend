package com.fonepay.devportal.modules.notification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fonepay.devportal.modules.notification.event.BroadcastCancelledEvent;
import com.fonepay.devportal.modules.notification.event.BroadcastCreatedEvent;
import com.fonepay.devportal.modules.notification.event.BroadcastUpdatedEvent;
import com.fonepay.devportal.modules.notification.service.BroadcastSseService;
import com.fonepay.devportal.modules.notification.webhook.BroadcastWebhookClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BroadcastEventListener {

    private final BroadcastSseService broadcastSseService;
    private final BroadcastWebhookClient broadcastWebhookClient;

    @EventListener
    public void handleBroadcastCreated(BroadcastCreatedEvent event) {
        log.info("Handling BroadcastCreatedEvent for broadcast [{}]", event.getBroadcast().getId());
        broadcastSseService.sendBroadcast(event.getBroadcast(), "BROADCAST_CREATED");
        broadcastWebhookClient.sendBroadcastWebhook(event.getBroadcast());
    }

    @EventListener
    public void handleBroadcastUpdated(BroadcastUpdatedEvent event) {
        log.info("Handling BroadcastUpdatedEvent for broadcast [{}]", event.getBroadcast().getId());
        broadcastSseService.sendBroadcast(event.getBroadcast(), "BROADCAST_UPDATED");
    }

    @EventListener
    public void handleBroadcastCancelled(BroadcastCancelledEvent event) {
        log.info("Handling BroadcastCancelledEvent for broadcast [{}]", event.getBroadcastId());
        broadcastSseService.sendCancellation(event.getBroadcastId());
    }
}
