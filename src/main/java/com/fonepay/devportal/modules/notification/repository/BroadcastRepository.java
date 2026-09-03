package com.fonepay.devportal.modules.notification.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.notification.document.Broadcast;
import com.fonepay.devportal.modules.notification.enums.BroadcastStatus;
import com.fonepay.devportal.modules.notification.enums.BroadcastTargetRole;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, String>, JpaSpecificationExecutor<Broadcast> {

    @Query("""
            SELECT b FROM Broadcast b
            WHERE b.status = :status
              AND b.targetRole IN :targetRoles
              AND b.startsAt <= :now
              AND (b.expiresAt IS NULL OR b.expiresAt > :now)
            """)
    List<Broadcast> findActiveForRoles(
            @Param("status") BroadcastStatus status,
            @Param("targetRoles") Collection<BroadcastTargetRole> targetRoles,
            @Param("now") Instant now);

    List<Broadcast> findAllByStatusAndExpiresAtBefore(BroadcastStatus status, Instant now);
}
