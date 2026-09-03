package com.fonepay.devportal.modules.user.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.modules.user.document.User;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("""
            DELETE FROM User u
            WHERE u.emailVerified = false
              AND u.createdAt < :cutoff
              AND NOT EXISTS (
                  SELECT 1 FROM UserRole ur
                  WHERE ur.user = u
                    AND ur.role.roleName IN :excludedRoles
              )
            """)
    void deleteByEmailVerifiedFalseAndCreatedAtBeforeAndRolesRoleNameNotIn(
            @Param("cutoff") Instant cutoff,
            @Param("excludedRoles") List<String> excludedRoles);
}
