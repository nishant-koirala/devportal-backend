package com.fonepay.devportal.modules.profile.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String productId;
    private boolean subscribed;
    private List<String> subscribedProductIds;
}
