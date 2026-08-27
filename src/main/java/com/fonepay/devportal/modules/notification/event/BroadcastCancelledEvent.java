package com.fonepay.devportal.modules.notification.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadcastCancelledEvent {
    private final String broadcastId;
}
