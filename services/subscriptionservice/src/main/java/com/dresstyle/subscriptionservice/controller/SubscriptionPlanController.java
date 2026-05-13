package com.dresstyle.subscriptionservice.controller;

import com.dresstyle.subscriptionservice.dto.SubscriptionPlanResponse;
import com.dresstyle.subscriptionservice.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription/plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/{planId}")
    public ResponseEntity<SubscriptionPlanResponse> getPlan(@PathVariable String planId) {
        return ResponseEntity.ok(planService.getPlanById(planId));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SubscriptionPlanResponse> getPlanByName(@PathVariable String name) {
        return ResponseEntity.ok(planService.getPlanByName(name));
    }
}
