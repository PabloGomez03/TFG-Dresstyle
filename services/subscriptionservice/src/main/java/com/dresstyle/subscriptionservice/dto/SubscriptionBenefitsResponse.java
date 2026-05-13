package com.dresstyle.subscriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionBenefitsResponse {
    private boolean hasFreeShipping;
    private int discountPercentage;
    private String planName;
}
