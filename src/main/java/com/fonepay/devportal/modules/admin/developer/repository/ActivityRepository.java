package com.fonepay.devportal.modules.admin.developer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.fonepay.devportal.common.constant.enums.ActivityType;
import com.fonepay.devportal.modules.admin.developer.document.Activity;

/** Loads one page of a developer's events from Mongo (not the full history). */
@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    /** All activity types for this user, sliced by page/size/sort. */
    Page<Activity> findByUserId(String userId, Pageable pageable);

    /** Same, filtered by type (used for login-history with type=LOGIN). */
    Page<Activity> findByUserIdAndType(String userId, ActivityType type, Pageable pageable);
}
