package com.dresstyle.subscriptionservice.repository;

import com.dresstyle.subscriptionservice.model.SubscriptionPlan;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends MongoRepository<SubscriptionPlan, String> {
    Optional<SubscriptionPlan> findByName(String name);
}
