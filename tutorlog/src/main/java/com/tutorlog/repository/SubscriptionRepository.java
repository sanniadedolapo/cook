package com.tutorlog.repository;

import com.tutorlog.model.Subscription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    Optional<Subscription> findByUserIdAndStatus(String userId, Subscription.Status status);

    List<Subscription> findByUserId(String userId);

    // Used by the scheduled expiry job
    List<Subscription> findByStatusAndExpiryDateBefore(
            Subscription.Status status, LocalDate date);
}
