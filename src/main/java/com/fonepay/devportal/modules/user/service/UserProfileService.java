package com.fonepay.devportal.modules.user.service;

import com.fonepay.devportal.modules.user.dto.request.EmailChangeRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdatePasswordRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdateProfileRequest;
import com.fonepay.devportal.modules.user.dto.response.DeveloperDashboardResponse;
import com.fonepay.devportal.modules.user.dto.response.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(String userId);

    DeveloperDashboardResponse getDashboard(String userId);

    void updateProfile(String userId, UpdateProfileRequest request);

    void updatePassword(String userId, UpdatePasswordRequest request);

    void requestEmailChange(String userId, EmailChangeRequest request);

    void verifyEmailChange(String token);
}
