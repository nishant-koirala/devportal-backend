package com.fonepay.devportal.modules.notification.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.notification.document.UserBroadcastInteraction;

@Repository
public interface UserBroadcastInteractionRepository extends MongoRepository<UserBroadcastInteraction, String> {

    Optional<UserBroadcastInteraction> findByUserIdAndBroadcastId(String userId, String broadcastId);

    List<UserBroadcastInteraction> findAllByUserIdAndBroadcastIdIn(String userId, Collection<String> broadcastIds);

    List<UserBroadcastInteraction> findAllByUserId(String userId);

    long countByBroadcastIdAndIsReadTrue(String broadcastId);

    long countByBroadcastIdAndIsDismissedTrue(String broadcastId);
}
