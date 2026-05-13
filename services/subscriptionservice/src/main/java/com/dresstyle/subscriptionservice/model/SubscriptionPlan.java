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
    private String name; // "Basic", "Premium"
    private String description;
    private double price; // Precio mensual en EUR
    private int discountPercentage; // 0 para Basic, 5 para Premium
    private boolean freeShipping; // true para ambos
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
