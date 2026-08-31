package com.fonepay.devportal.modules.user.service;

import com.fonepay.devportal.modules.user.dto.request.EmailChangeRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdatePasswordRequest;
import com.fonepay.devportal.modules.user.dto.request.UpdateProfileRequest;

public interface UserProfileService {

    void updateProfile(String userId, UpdateProfileRequest request);

    void updatePassword(String userId, UpdatePasswordRequest request);

    void requestEmailChange(String userId, EmailChangeRequest request);

    void verifyEmailChange(String token);
}
