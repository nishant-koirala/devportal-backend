package com.fonepay.devportal.modules.profile.service;

import java.util.List;

import com.fonepay.devportal.modules.profile.dto.response.SubscriptionResponse;

public interface UserSubscriptionService {

    SubscriptionResponse subscribeProduct(String userId, String productId);

    SubscriptionResponse unsubscribeProduct(String userId, String productId);

    List<String> getSubscribedProductIds(String userId);
}
