package com.tutorlog.dto;

import com.tutorlog.model.Subscription;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SubscriptionDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Plan is required")
        private Subscription.Plan plan;
    }

    @Data
    public static class Response {
        private String id;
        private String userId;
        private Subscription.Plan plan;
        private Subscription.Status status;
        private LocalDate startDate;
        private LocalDate expiryDate;
        private LocalDateTime createdAt;
        private boolean active;

        public static Response from(Subscription sub) {
            Response r = new Response();
            r.setId(sub.getId());
            r.setUserId(sub.getUserId());
            r.setPlan(sub.getPlan());
            r.setStatus(sub.getStatus());
            r.setStartDate(sub.getStartDate());
            r.setExpiryDate(sub.getExpiryDate());
            r.setCreatedAt(sub.getCreatedAt());
            r.setActive(sub.isActive());
            return r;
        }
    }
}
