package com.fonepay.devportal.modules.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a new 6-digit OTP code.
     *
     * @return the generated OTP code (plain text, for sending via email)
     */
    public String generateOtpCode() {
        // Generate 6-digit numeric OTP
        int otpNumber = secureRandom.nextInt(900000) + 100000; // 100000 to 999999
        return String.valueOf(otpNumber);
    }

    /**
     * Hash an OTP code using SHA-256 for secure storage.
     *
     * @param otpCode the plain text OTP code
     * @return the SHA-256 hash hex string
     */
    public String hashOtp(String otpCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(otpCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BadRequestException("Failed to hash OTP: " + e.getMessage());
        }
    }

    /**
     * Verify the provided OTP code against the stored hash.
     *
     * @param providedCode the OTP code provided by the user
     * @param storedHash the stored SHA-256 hash
     * @return true if the code matches the hash
     */
    public boolean verifyOtpCode(String providedCode, String storedHash) {
        if (providedCode == null || storedHash == null) {
            return false;
        }
        return hashOtp(providedCode).equals(storedHash);
    }
}