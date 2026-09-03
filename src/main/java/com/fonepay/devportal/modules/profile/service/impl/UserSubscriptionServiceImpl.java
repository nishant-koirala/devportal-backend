package com.fonepay.devportal.modules.profile.service.impl;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.common.exception.ResourceNotFoundException;
import com.fonepay.devportal.modules.admin.developer.service.ActivityRecordingService;
import com.fonepay.devportal.modules.cms.document.Product;
import com.fonepay.devportal.modules.cms.enums.ProductStatus;
import com.fonepay.devportal.modules.cms.repository.ProductRepository;
import com.fonepay.devportal.modules.profile.dto.response.SubscriptionResponse;
import com.fonepay.devportal.modules.profile.service.UserSubscriptionService;
import com.fonepay.devportal.modules.user.document.UserProduct;
import com.fonepay.devportal.modules.user.repository.UserProductRepository;
import com.fonepay.devportal.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    private final UserRepository userRepository;
    private final UserProductRepository userProductRepository;
    private final ProductRepository productRepository;
    private final ActivityRecordingService activityRecordingService;
    private final Clock clock;

    @Override
    @Transactional
    public SubscriptionResponse subscribeProduct(String userId, String productId) {
        validateProductId(productId);
        ensureUserExists(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new BadRequestException("Cannot subscribe to unpublished product: " + product.getName());
        }

        if (!userProductRepository.existsByUserIdAndProductId(userId, productId)) {
            userProductRepository.save(UserProduct.builder()
                    .userId(userId)
                    .productId(productId)
                    .selectedAt(clock.instant())
                    .build());
            activityRecordingService.record(userId, ActivityType.PRODUCT_ADDED);
            log.info("User [{}] successfully subscribed to product [{}]", userId, productId);
        } else {
            log.info("User [{}] already subscribed to product [{}]", userId, productId);
        }

        return SubscriptionResponse.builder()
                .productId(productId)
                .subscribed(true)
                .subscribedProductIds(getSubscribedProductIds(userId))
                .build();
    }

    @Override
    @Transactional
    public SubscriptionResponse unsubscribeProduct(String userId, String productId) {
        validateProductId(productId);
        ensureUserExists(userId);

        if (userProductRepository.existsByUserIdAndProductId(userId, productId)) {
            userProductRepository.deleteByUserIdAndProductId(userId, productId);
            activityRecordingService.record(userId, ActivityType.PRODUCT_REMOVED);
            log.info("User [{}] successfully unsubscribed from product [{}]", userId, productId);
        } else {
            log.info("User [{}] was not subscribed to product [{}]", userId, productId);
        }

        return SubscriptionResponse.builder()
                .productId(productId)
                .subscribed(false)
                .subscribedProductIds(getSubscribedProductIds(userId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getSubscribedProductIds(String userId) {
        ensureUserExists(userId);
        return userProductRepository.findByUserId(userId).stream()
                .map(UserProduct::getProductId)
                .toList();
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
    }

    private void validateProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new BadRequestException("Product ID must not be blank");
        }
    }
}
