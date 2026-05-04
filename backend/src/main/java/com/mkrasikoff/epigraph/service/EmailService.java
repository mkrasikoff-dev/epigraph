package com.mkrasikoff.epigraph.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a 6-digit verification code to the given email address.
     */
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject("Ваш код подтверждения Epigraph");
            msg.setText("""
                    Добро пожаловать в Epigraph!
                    
                    Ваш код подтверждения: %s
                    
                    Код действителен 15 минут.
                    Если вы не регистрировались — просто проигнорируйте это письмо.
                    """.formatted(code));
            mailSender.send(msg);
        } catch (MailException e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо. Попробуйте позже.");
        }
    }
}
