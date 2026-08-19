package com.fonepay.devportal.modules.notification.service;

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

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public void sendVerificationEmail(String toEmail, String verificationUrl) {
        log.info("Sending verification email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify your DevPortal account");
            message.setText("Welcome to DevPortal!\n\nPlease click the link below to verify your email address:\n"
                    + verificationUrl + "\n\nThis link will expire in 24 hours.");

            javaMailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email");
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetUrl) {
        log.info("Sending password reset email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset your DevPortal password");
            message.setText("You requested a password reset.\n\n"
                    + "Click the link below to set a new password:\n"
                    + resetUrl
                    + "\n\nThis link will expire in 1 hour. If you did not request this, you can ignore this email.");

            javaMailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email");
        }
    }

    public void sendOtpEmail(String toEmail, String otpCode, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code for Fonepay Developer Portal");
            message.setText(buildOtpEmailBody(otpCode, userName));
            javaMailSender.send(message);
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
