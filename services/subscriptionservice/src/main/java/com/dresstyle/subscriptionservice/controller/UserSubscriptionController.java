package com.dresstyle.subscriptionservice.controller;

import com.dresstyle.subscriptionservice.dto.SubscribeRequest;
import com.dresstyle.subscriptionservice.dto.SubscriptionBenefitsResponse;
import com.dresstyle.subscriptionservice.dto.UserSubscriptionResponse;
import com.dresstyle.subscriptionservice.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class UserSubscriptionController {

    private final UserSubscriptionService subscriptionService;

    
    @GetMapping("/my-subscription")
    public ResponseEntity<UserSubscriptionResponse> getMySubscription(Authentication authentication) {
        String userId = authentication.getName();
        UserSubscriptionResponse subscription = subscriptionService.getActiveSubscription(userId);

        if (subscription == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(subscription);
    }

    
    @GetMapping("/history")
    public ResponseEntity<List<UserSubscriptionResponse>> getSubscriptionHistory(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(subscriptionService.getSubscriptionHistory(userId));
    }

    
    @PostMapping("/subscribe")
    public ResponseEntity<UserSubscriptionResponse> subscribe(
            Authentication authentication,
            @RequestBody SubscribeRequest request) {
        String userId = authentication.getName();
        UserSubscriptionResponse subscription = subscriptionService.subscribe(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelSubscription(Authentication authentication) {
        String userId = authentication.getName();
        subscriptionService.cancelSubscription(userId);
        return ResponseEntity.ok(Map.of("message", "Suscripción cancelada exitosamente"));
    }

    
    @GetMapping("/benefits")
    public ResponseEntity<SubscriptionBenefitsResponse> getBenefits(Authentication authentication) {
        String userId = authentication.getName();
        SubscriptionBenefitsResponse benefits = subscriptionService.getUserBenefits(userId);
        return ResponseEntity.ok(benefits);
    }
}
