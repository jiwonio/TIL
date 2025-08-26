package com.example.til.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8088}")
    private String baseUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your email";
        String verifyUrl = baseUrl + "/auth/verify?token=" + token;
        String body = "<p>Thanks for registering.</p>" +
                "<p>Please verify your email by clicking the link below:</p>" +
                "<p><a href='" + verifyUrl + "'>Verify Email</a></p>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("[MailService] Verification email sent to {}: {}", to, verifyUrl);
        } catch (MailException | MessagingException e) {
            // In dev, just log the link so user can click manually
            log.warn("[MailService] Failed to send email to {}. Link: {}. Error: {}", to, verifyUrl, e.getMessage());
        }
    }
}
