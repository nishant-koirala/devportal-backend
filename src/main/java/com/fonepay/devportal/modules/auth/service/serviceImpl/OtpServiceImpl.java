package com.fonepay.devportal.modules.auth.service.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.BadRequestException;
import com.fonepay.devportal.modules.auth.service.OtpService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OtpServiceImpl implements OtpService {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generateOtpCode() {
        int otpNumber = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(otpNumber);
    }

    @Override
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

    @Override
    public boolean verifyOtpCode(String providedCode, String storedHash) {
        if (providedCode == null || storedHash == null) {
            return false;
        }
        return hashOtp(providedCode).equals(storedHash);
    }
}
