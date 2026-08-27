package com.fonepay.devportal.modules.notification.event;

import com.fonepay.devportal.modules.notification.document.Broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BroadcastUpdatedEvent {
    private final Broadcast broadcast;
}
