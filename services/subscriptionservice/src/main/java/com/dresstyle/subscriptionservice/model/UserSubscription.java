package com.dresstyle.subscriptionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "user_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {
    @Id
    private String id;
    private String userId;
    private String planId; 
    private String planName;
    private String status; 
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime renewalDate;
    private boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
