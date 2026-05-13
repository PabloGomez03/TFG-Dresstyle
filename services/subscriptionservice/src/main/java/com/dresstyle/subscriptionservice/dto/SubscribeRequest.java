package com.dresstyle.subscriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeRequest {
    private String planId; // ID del plan (Basic o Premium)
    private boolean autoRenew;
}
