package com.tutorlog.service;

import com.tutorlog.dto.SubscriptionDto;
import com.tutorlog.model.Subscription;
import com.tutorlog.model.User;
import com.tutorlog.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AuthService authService;

    // ── Subscribe ────────────────────────────────────────────
    public SubscriptionDto.Response subscribe(SubscriptionDto.CreateRequest request) {
        User user = authService.getCurrentUser();

        // Cancel any existing active subscription first
        subscriptionRepository
                .findByUserIdAndStatus(user.getId(), Subscription.Status.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(Subscription.Status.CANCELLED);
                    existing.setCancelledAt(LocalDateTime.now());
                    subscriptionRepository.save(existing);
                });

        LocalDate start = LocalDate.now();
        LocalDate expiry = switch (request.getPlan()) {
            case BASIC, PRO -> start.plusMonths(1);
            case ANNUAL_PRO  -> start.plusYears(1);
        };

        Subscription subscription = Subscription.builder()
                .userId(user.getId())
                .plan(request.getPlan())
                .status(Subscription.Status.ACTIVE)
                .startDate(start)
                .expiryDate(expiry)
                .createdAt(LocalDateTime.now())
                .build();

        return SubscriptionDto.Response.from(subscriptionRepository.save(subscription));
    }

    // ── Get current subscription ─────────────────────────────
    public SubscriptionDto.Response getCurrentSubscription() {
        User user = authService.getCurrentUser();
        return subscriptionRepository
                .findByUserIdAndStatus(user.getId(), Subscription.Status.ACTIVE)
                .map(SubscriptionDto.Response::from)
                .orElse(null);
    }

    // ── Get subscription history ──────────────────────────────
    public List<SubscriptionDto.Response> getSubscriptionHistory() {
        User user = authService.getCurrentUser();
        return subscriptionRepository.findByUserId(user.getId()).stream()
                .map(SubscriptionDto.Response::from)
                .collect(Collectors.toList());
    }

    // ── Cancel ───────────────────────────────────────────────
    public SubscriptionDto.Response cancelSubscription() {
        User user = authService.getCurrentUser();
        Subscription sub = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), Subscription.Status.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        sub.setStatus(Subscription.Status.CANCELLED);
        sub.setCancelledAt(LocalDateTime.now());
        return SubscriptionDto.Response.from(subscriptionRepository.save(sub));
    }

    // ── Check if user has active subscription ────────────────
    public boolean hasActiveSubscription(String userId) {
        return subscriptionRepository
                .findByUserIdAndStatus(userId, Subscription.Status.ACTIVE)
                .map(Subscription::isActive)
                .orElse(false);
    }

    // ── Scheduled expiry job ─────────────────────────────────
    /**
     * Runs every day at midnight.
     * Finds all ACTIVE subscriptions whose expiry date has passed
     * and marks them as EXPIRED — this is the core access restriction mechanism.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndExpiryDateBefore(Subscription.Status.ACTIVE, LocalDate.now());

        if (!expired.isEmpty()) {
            logger.info("Expiring {} subscriptions", expired.size());
            expired.forEach(sub -> {
                sub.setStatus(Subscription.Status.EXPIRED);
                logger.info("Expired subscription {} for user {}", sub.getId(), sub.getUserId());
            });
            subscriptionRepository.saveAll(expired);
        }
    }
}
