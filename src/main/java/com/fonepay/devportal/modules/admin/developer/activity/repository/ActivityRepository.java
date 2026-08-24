package com.fonepay.devportal.modules.admin.developer.activity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.modules.admin.developer.activity.document.Activity;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    Page<Activity> findByUserId(String userId, Pageable pageable);

    Page<Activity> findByUserIdAndType(String userId, ActivityType type, Pageable pageable);
}
