package com.fonepay.devportal.modules.notification.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

@Repository
public interface BroadcastRepository extends MongoRepository<Broadcast, String> {

    @Query("{ 'status': ?0, 'target_role': { $in: ?1 }, 'starts_at': { $lte: ?2 }, $or: [ { 'expires_at': null }, { 'expires_at': { $gt: ?2 } } ] }")
    List<Broadcast> findActiveForRoles(BroadcastStatus status, Collection<BroadcastTargetRole> targetRoles, Instant now);

    List<Broadcast> findAllByStatusAndExpiresAtBefore(BroadcastStatus status, Instant now);
}
