package com.dresstyle.subscriptionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {
    @Id
    private String id;
    private String name; 
    private String description;
    private double price; 
    private int discountPercentage; 
    private boolean freeShipping; 
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
