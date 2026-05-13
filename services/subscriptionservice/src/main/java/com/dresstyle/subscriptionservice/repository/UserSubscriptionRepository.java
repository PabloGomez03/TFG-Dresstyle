package com.dresstyle.subscriptionservice.repository;

import com.dresstyle.subscriptionservice.model.UserSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends MongoRepository<UserSubscription, String> {
    Optional<UserSubscription> findByUserIdAndStatus(String userId, String status);
    Optional<UserSubscription> findByUserId(String userId);
    List<UserSubscription> findAllByUserId(String userId);
}
