package com.fonepay.devportal.modules.profile.service.impl;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.admin.developer.service.ActivityRecordingService;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.profile.dto.response.SubscriptionResponse;
import com.fonepay.devportal.modules.profile.service.UserSubscriptionService;
import com.fonepay.devportal.modules.user.document.User;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ActivityRecordingService activityRecordingService;
    private final Clock clock;

    @Override
    public SubscriptionResponse subscribeProduct(String userId, String productId) {
        validateProductId(productId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BadRequestException("Cannot subscribe to unpublished product: " + product.getName());
        }

        List<String> subscriptions = user.getSubscribedProductIds();
        if (!subscriptions.contains(productId)) {
            subscriptions.add(productId);
            user.setUpdatedAt(clock.instant());
            userRepository.save(user);

            activityRecordingService.record(userId, ActivityType.PRODUCT_ADDED);
            log.info("User [{}] successfully subscribed to product [{}]", userId, productId);
        } else {
            log.info("User [{}] already subscribed to product [{}]", userId, productId);
        }

        return SubscriptionResponse.builder()
                .productId(productId)
                .subscribed(true)
                .subscribedProductIds(subscriptions)
                .build();
    }

    @Override
    public SubscriptionResponse unsubscribeProduct(String userId, String productId) {
        validateProductId(productId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<String> subscriptions = user.getSubscribedProductIds();
        if (subscriptions.remove(productId)) {
            user.setUpdatedAt(clock.instant());
            userRepository.save(user);

            activityRecordingService.record(userId, ActivityType.PRODUCT_REMOVED);
            log.info("User [{}] successfully unsubscribed from product [{}]", userId, productId);
        } else {
            log.info("User [{}] was not subscribed to product [{}]", userId, productId);
        }

        return SubscriptionResponse.builder()
                .productId(productId)
                .subscribed(false)
                .subscribedProductIds(subscriptions)
                .build();
    }

    @Override
    public List<String> getSubscribedProductIds(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return user.getSubscribedProductIds();
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
    }
}
