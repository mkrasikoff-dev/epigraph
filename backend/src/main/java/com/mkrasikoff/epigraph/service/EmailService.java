package com.mkrasikoff.epigraph.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestClient restClient;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.resend.api-key}")
    private String apiKey;

    public EmailService() {
        this.restClient = RestClient.create();
    }

    /**
     * Sends a 6-digit verification code via Resend HTTP API.
     * Uses HTTPS (port 443) — works on all hosting providers including Railway.
     */
    public void sendVerificationCode(String to, String code) {
        Map<String, Object> body = Map.of(
                "from", from,
                "to", new String[]{to},
                "subject", "Ваш код подтверждения Epigraph",
                "text", """
                        Добро пожаловать в Epigraph!
                        
                        Ваш код подтверждения: %s
                        
                        Код действителен 15 минут.
                        Если вы не регистрировались — просто проигнорируйте это письмо.
                        """.formatted(code)
        );

        try {
            restClient.post()
                    .uri(RESEND_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Verification email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Не удалось отправить письмо. Попробуйте позже.");
        }
    }

    /**
     * Sends a password-reset link via Resend HTTP API.
     */
    public void sendPasswordResetLink(String to, String resetLink) {
        Map<String, Object> body = Map.of(
                "from", from,
                "to", new String[]{to},
                "subject", "Сброс пароля Epigraph",
                "text", """
                    Вы запросили смену пароля в Epigraph.
                    
                    Перейдите по ссылке, чтобы установить новый пароль:
                    %s
                    
                    Ссылка действительна 30 минут.
                    Если вы не запрашивали смену пароля — просто проигнорируйте это письмо.
                    """.formatted(resetLink)
        );

        try {
            restClient.post()
                    .uri(RESEND_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Password reset email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());

            throw new RuntimeException("Не удалось отправить письмо. Попробуйте позже.");
        }
    }
}
