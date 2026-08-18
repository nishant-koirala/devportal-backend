package com.fonepay.devportal.modules.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    /**
     * Send OTP email to the user.
     *
     * @param toEmail recipient email address
     * @param otpCode the 6-digit OTP code
     * @param userName user's full name (optional, for personalization)
     */
    public void sendOtpEmail(String toEmail, String otpCode, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code for Fonepay Developer Portal");
            message.setText(buildOtpEmailBody(otpCode, userName));
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailBody(String otpCode, String userName) {
        String greeting = (userName != null && !userName.trim().isEmpty())
                ? "Hello " + userName + ",\n\n"
                : "Hello,\n\n";

        return greeting +
                "Your One-Time Password (OTP) for Fonepay Developer Portal login is:\n\n" +
                "    " + otpCode + "\n\n" +
                "This code will expire in " + otpExpirationMinutes + " minutes.\n\n" +
                "If you did not request this code, please ignore this email or contact support.\n\n" +
                "Regards,\n" +
                "Fonepay Developer Portal Team";
    }
}