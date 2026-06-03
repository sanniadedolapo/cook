package com.tutorlog.controller;

import com.tutorlog.dto.SubscriptionDto;
import com.tutorlog.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * GET /api/subscriptions/current
     * Returns the calling user's active subscription, or 204 if none.
     */
    @GetMapping("/current")
    public ResponseEntity<SubscriptionDto.Response> getCurrentSubscription() {
        SubscriptionDto.Response sub = subscriptionService.getCurrentSubscription();
        return sub != null
                ? ResponseEntity.ok(sub)
                : ResponseEntity.noContent().build();
    }

    /**
     * GET /api/subscriptions/history
     * Returns all subscriptions (including expired/cancelled) for the calling user.
     */
    @GetMapping("/history")
    public ResponseEntity<List<SubscriptionDto.Response>> getSubscriptionHistory() {
        return ResponseEntity.ok(subscriptionService.getSubscriptionHistory());
    }

    /**
     * POST /api/subscriptions
     * Body: { plan: "BASIC" | "PRO" | "ANNUAL_PRO" }
     * Creates a new subscription. Cancels any existing active one first.
     */
    @PostMapping
    public ResponseEntity<SubscriptionDto.Response> subscribe(
            @Valid @RequestBody SubscriptionDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(subscriptionService.subscribe(request));
    }

    /**
     * DELETE /api/subscriptions/cancel
     * Cancels the calling user's active subscription.
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<SubscriptionDto.Response> cancelSubscription() {
        return ResponseEntity.ok(subscriptionService.cancelSubscription());
    }
}
