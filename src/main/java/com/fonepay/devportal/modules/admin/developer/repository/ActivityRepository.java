package com.fonepay.devportal.modules.admin.developer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.modules.admin.developer.document.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, String> {

    Page<Activity> findByUserId(String userId, Pageable pageable);

    Page<Activity> findByUserIdAndType(String userId, ActivityType type, Pageable pageable);
}
