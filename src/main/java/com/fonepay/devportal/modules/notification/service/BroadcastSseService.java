package com.fonepay.devportal.modules.notification.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.user.document.User;

public interface BroadcastSseService {

    SseEmitter subscribe(User user);

    void sendBroadcast(Broadcast broadcast, String eventType);

    void sendCancellation(String broadcastId);

    void sendHeartbeat();

    int getActiveConnectionCount();
}
