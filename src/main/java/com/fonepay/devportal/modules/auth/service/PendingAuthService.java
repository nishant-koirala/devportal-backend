package com.fonepay.devportal.modules.auth.service;

import com.fonepay.devportal.modules.auth.document.PendingAuth;

public interface PendingAuthService {

    /**
     * Create a new pending authentication record with OTP hash.
     *
     * @param userId the user ID
     * @param otpHash the hashed OTP code
     * @param expirationMinutes the OTP expiration in minutes
     * @return the created PendingAuth record
     */
    PendingAuth createPendingAuth(String userId, String otpHash, int expirationMinutes);

    /**
     * Find a pending authentication record by ID.
     *
     * @param id the pending auth ID
     * @return the pending auth record, or empty if not found
     */
    java.util.Optional<PendingAuth> findById(String id);

    /**
     * Find a pending authentication record by user ID with PENDING status.
     *
     * @param userId the user ID
     * @return the pending auth record, or empty if not found
     */
    java.util.Optional<PendingAuth> findPendingByUserId(String userId);

    /**
     * Verify the provided OTP code against the stored hash.
     *
     * @param pendingAuth the pending auth record
     * @param providedCode the OTP code provided by the user
     * @param maxAttempts maximum allowed attempts
     * @return true if OTP is valid and verified, false otherwise
     */
    boolean verifyOtp(PendingAuth pendingAuth, String providedCode, int maxAttempts);

    /**
     * Mark pending auth as verified.
     *
     * @param pendingAuth the pending auth record
     */
    void markVerified(PendingAuth pendingAuth);

    /**
     * Delete a pending auth record (e.g., after successful verification or cleanup).
     *
     * @param pendingAuth the pending auth record to delete
     */
    void deletePendingAuth(PendingAuth pendingAuth);
}