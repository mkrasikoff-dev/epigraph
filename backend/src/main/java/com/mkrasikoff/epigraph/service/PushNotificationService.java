package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.dto.PushSubscriptionDto;
import com.mkrasikoff.epigraph.model.PushSubscription;
import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {

    private final PushSubscriptionRepository subRepo;
    private final QuoteService quoteService;
    private final ObjectMapper objectMapper;

    @Getter
    @Value("${app.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${app.vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${app.vapid.subject:mailto:epigraph@example.com}")
    private String vapidSubject;

    /**
     * null means VAPID keys not configured → push disabled
     */
    private nl.martijndwars.webpush.PushService webPushClient;

    @PostConstruct
    public void init() {
        if (vapidPublicKey == null || vapidPublicKey.isBlank()) {
            log.warn("VAPID keys not configured — push notifications disabled");
            return;
        }
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            webPushClient = new nl.martijndwars.webpush.PushService(
                    vapidPublicKey, vapidPrivateKey, vapidSubject);
            log.info("Push notification service ready");
        } catch (Exception e) {
            log.error("Failed to initialize push service — push disabled", e);
        }
    }

    public void saveSubscription(Long userId, PushSubscriptionDto dto) {
        PushSubscription sub = subRepo.findByEndpoint(dto.getEndpoint())
                .orElse(new PushSubscription());

        sub.setUserId(userId);
        sub.setEndpoint(dto.getEndpoint());
        sub.setP256dh(dto.getKeys().getP256dh());
        sub.setAuth(dto.getKeys().getAuth());
        sub.setIntervalHours(dto.getIntervalHours() > 0 ? dto.getIntervalHours() : 24);
        sub.setLastSent(0L); // fire at next scheduler tick
        if (sub.getSubscribedAt() == 0L) {
            sub.setSubscribedAt(System.currentTimeMillis());
        }

        subRepo.save(sub);
        log.info("Push subscription saved for userId = {}, interval = {}h", userId, sub.getIntervalHours());
    }

    public void deleteSubscription(Long userId, String endpoint) {
        subRepo.deleteByEndpointAndUserId(endpoint, userId);
        log.info("Push subscription removed for userId = {}", userId);
    }

    public void updateInterval(Long userId, int intervalHours) {
        subRepo.findByUserId(userId).forEach(sub -> {
            sub.setIntervalHours(intervalHours);
            subRepo.save(sub);
        });
        log.info("Notification interval updated to {}h for userId = {}", intervalHours, userId);
    }

    @Scheduled(cron = "0 0 * * * *")
    public void sendDueNotifications() {
        if (webPushClient == null) return;

        long now = System.currentTimeMillis();
        List<PushSubscription> due = subRepo.findDue(now);
        if (due.isEmpty()) return;

        log.info("Sending push to {} subscription(s)", due.size());

        for (PushSubscription sub : due) {
            try {
                Optional<Quote> qodOpt = quoteService.getQod(sub.getUserId());
                if (qodOpt.isEmpty()) continue;

                String payload = buildPayload(qodOpt.get());
                HttpResponse response = webPushClient.send(new Notification(
                        new Subscription(sub.getEndpoint(),
                                new Subscription.Keys(sub.getP256dh(), sub.getAuth())),
                        Arrays.toString(payload.getBytes(StandardCharsets.UTF_8))
                ));

                int status = response.getStatusLine().getStatusCode();

                if (status == 200 || status == 201) {
                    sub.setLastSent(now);
                    subRepo.save(sub);
                    log.debug("Push delivered to userId = {}", sub.getUserId());
                } else if (status == 410 || status == 404) {
                    // Subscription expired — browser revoked it
                    subRepo.delete(sub);
                    log.info("Expired subscription deleted for userId = {}", sub.getUserId());
                } else {
                    log.warn("Push returned HTTP {} for userId = {}", status, sub.getUserId());
                }

            } catch (Exception e) {
                log.error("Push failed for userId = {}", sub.getUserId(), e);
            }
        }
    }

    private String buildPayload(Quote q) throws Exception {
        String body = (q.getAuthor() != null && !q.getAuthor().isBlank())
                ? "«" + q.getText() + "»\n— " + q.getAuthor()
                : "«" + q.getText() + "»";

        return objectMapper.writeValueAsString(Map.of(
                "title", "Цитата дня · Epigraph",
                "body", body,
                "icon", "/icon.png"
        ));
    }
}
