package com.fonepay.devportal.modules.notification.webhook;

import com.fonepay.devportal.modules.notification.document.Broadcast;

public interface BroadcastWebhookClient {

    void sendBroadcastWebhook(Broadcast broadcast);
}
