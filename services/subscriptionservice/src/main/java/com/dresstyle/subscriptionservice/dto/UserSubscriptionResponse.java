package com.dresstyle.subscriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionResponse {
    private String id;
    private String userId;
    private SubscriptionPlanResponse plan;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime renewalDate;
    private boolean autoRenew;
}
