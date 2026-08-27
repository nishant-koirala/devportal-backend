package com.fonepay.devportal.modules.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fonepay.devportal.common.exception.EmailSendException;

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
            // Avoid raw RuntimeException so the client gets a generic 503, not SMTP details.
            throw new EmailSendException("Failed to send email", e);
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
            throw new EmailSendException("Failed to send email", e);
        }
    }

    public void sendInviteEmail(String toEmail, String inviteUrl, String role, String departmentName,
            String fullName) {
        log.info("Sending staff invite email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("You have been invited to Fonepay Developer Portal");
            message.setText(buildInviteEmailBody(inviteUrl, role, departmentName, fullName));
            javaMailSender.send(message);
            log.info("Invite email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send invite email to: {}", toEmail, e);
            throw new EmailSendException("Failed to send email", e);
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
            throw new EmailSendException("Failed to send OTP email", e);
        }
    }

    private String buildInviteEmailBody(String inviteUrl, String role, String departmentName, String fullName) {
        String greeting = (fullName != null && !fullName.trim().isEmpty())
                ? "Hello " + fullName.trim() + ",\n\n"
                : "Hello,\n\n";

        String departmentLine = (departmentName != null && !departmentName.trim().isEmpty())
                ? " in the " + departmentName.trim() + " department"
                : "";

        return greeting
                + "You have been invited to join the Fonepay Developer Portal as " + role + departmentLine + ".\n\n"
                + "Click the link below to set your password and activate your account:\n"
                + inviteUrl + "\n\n"
                + "This link will expire in 48 hours. If you were not expecting this invitation, you can ignore this email.\n\n"
                + "Regards,\n"
                + "Fonepay Developer Portal Team";
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
