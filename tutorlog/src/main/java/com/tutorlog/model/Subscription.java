package com.tutorlog.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;

    private String userId;

    private Plan plan;
    private Status status;

    private LocalDate startDate;
    private LocalDate expiryDate;

    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    public enum Plan {
        BASIC, PRO, ANNUAL_PRO
    }

    public enum Status {
        ACTIVE, EXPIRED, CANCELLED
    }

    public boolean isActive() {
        return status == Status.ACTIVE && expiryDate != null
                && !LocalDate.now().isAfter(expiryDate);
    }
}
