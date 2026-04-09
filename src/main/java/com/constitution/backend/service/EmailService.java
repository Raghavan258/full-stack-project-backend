package com.constitution.backend.service;

import com.constitution.backend.entity.OtpToken;
import com.constitution.backend.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final OtpTokenRepository otpTokenRepository;

    private static final int OTP_VALIDITY_MINUTES = 10;

    /** Generate a 6-digit OTP, persist it, and email it. */
    public void sendOtp(String email, String recipientName) {
        String otp = generateOtp();

        OtpToken token = OtpToken.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .build();
        otpTokenRepository.save(token);

        sendOtpEmail(email, recipientName, otp);
        log.info("OTP sent to {}", email);
    }

    /** Verify an OTP for the given email. */
    public boolean verifyOtp(String email, String otp) {
        OtpToken token = otpTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);

        if (token == null || token.isExpired() || !token.getOtp().equals(otp)) {
            return false;
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }

    /** Send a plain welcome email after successful registration. */
    public void sendWelcomeEmail(String email, String name) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Welcome to Know Your Constitution 🇮🇳");
            helper.setText(buildWelcomeHtml(name), true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            log.warn("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000; // always 6 digits
        return String.valueOf(num);
    }

    private void sendOtpEmail(String to, String name, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your OTP — Know Your Constitution");
            helper.setText(buildOtpHtml(name, otp), true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email delivery failed. Please try again.");
        }
    }

    private String buildOtpHtml(String name, String otp) {
        return """
            <div style="font-family:sans-serif;max-width:480px;margin:auto;background:#f9f9f9;padding:32px;border-radius:12px;">
              <div style="text-align:center;font-size:28px;">🇮🇳</div>
              <h2 style="color:#FF6B00;text-align:center;">Know Your Constitution</h2>
              <p>Hi <strong>%s</strong>,</p>
              <p>Your one-time password (OTP) for email verification is:</p>
              <div style="font-size:40px;font-weight:bold;letter-spacing:8px;text-align:center;
                          color:#1a1a2e;background:#fff;padding:20px;border-radius:8px;margin:24px 0;">
                %s
              </div>
              <p style="color:#666;">This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
              <hr style="border:none;border-top:1px solid #eee;margin:24px 0;"/>
              <p style="font-size:12px;color:#aaa;text-align:center;">
                If you did not request this, please ignore this email.
              </p>
            </div>
            """.formatted(name, otp);
    }

    private String buildWelcomeHtml(String name) {
        return """
            <div style="font-family:sans-serif;max-width:480px;margin:auto;padding:32px;">
              <h2 style="color:#FF6B00;">Welcome, %s! 🎉</h2>
              <p>Your account on <strong>Know Your Constitution</strong> has been verified.</p>
              <p>Start exploring articles, quizzes, and discussions to deepen your understanding of the Indian Constitution.</p>
              <p style="margin-top:24px;">— Team Know Your Constitution</p>
            </div>
            """.formatted(name);
    }

    /** Cleanup expired / used OTPs every hour */
    @Scheduled(fixedRate = 3_600_000)
    public void cleanupOtps() {
        otpTokenRepository.deleteExpiredAndUsed(LocalDateTime.now());
        log.debug("OTP cleanup executed");
    }
}
