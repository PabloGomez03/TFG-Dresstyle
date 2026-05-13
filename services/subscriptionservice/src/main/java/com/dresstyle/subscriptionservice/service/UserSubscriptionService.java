package com.dresstyle.subscriptionservice.service;

import com.dresstyle.subscriptionservice.dto.SubscribeRequest;
import com.dresstyle.subscriptionservice.dto.SubscriptionBenefitsResponse;
import com.dresstyle.subscriptionservice.dto.UserSubscriptionResponse;
import com.dresstyle.subscriptionservice.model.SubscriptionPlan;
import com.dresstyle.subscriptionservice.model.UserSubscription;
import com.dresstyle.subscriptionservice.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService planService;

    /**
     * Obtener la suscripción activa del usuario
     */
    public UserSubscriptionResponse getActiveSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, "active")
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Obtener cualquier suscripción del usuario
     */
    public UserSubscriptionResponse getUserSubscription(String userId) {
        return subscriptionRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    /**
     * Obtener el historial de suscripciones
     */
    public List<UserSubscriptionResponse> getSubscriptionHistory(String userId) {
        return subscriptionRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crear o actualizar suscripción del usuario
     */
    public UserSubscriptionResponse subscribe(String userId, SubscribeRequest request) {
        // Obtener el plan
        SubscriptionPlan plan = planService.getPlanModel(request.getPlanId());

        // Si el usuario ya tiene una suscripción activa, cancelarla primero
        subscriptionRepository.findByUserIdAndStatus(userId, "active")
                .ifPresent(sub -> {
                    sub.setStatus("cancelled");
                    sub.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(sub);
                });

        // Crear nueva suscripción
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusMonths(1); // Suscripción de 1 mes

        UserSubscription subscription = UserSubscription.builder()
                .userId(userId)
                .planId(plan.getId())
                .planName(plan.getName())
                .status("active")
                .startDate(now)
                .endDate(endDate)
                .renewalDate(endDate)
                .autoRenew(request.isAutoRenew())
                .createdAt(now)
                .updatedAt(now)
                .build();

        UserSubscription saved = subscriptionRepository.save(subscription);
        return mapToResponse(saved);
    }

    /**
     * Cancelar suscripción
     */
    public void cancelSubscription(String userId) {
        subscriptionRepository.findByUserIdAndStatus(userId, "active")
                .ifPresent(sub -> {
                    sub.setStatus("cancelled");
                    sub.setUpdatedAt(LocalDateTime.now());
                    subscriptionRepository.save(sub);
                });
    }

    /**
     * Verificar si el usuario tiene una suscripción activa
     */
    public boolean hasActiveSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, "active").isPresent();
    }

    /**
     * Obtener los beneficios del usuario (descuento, envío gratis, etc.)
     */
    public SubscriptionBenefitsResponse getUserBenefits(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, "active")
                .map(sub -> {
                    SubscriptionPlan plan = planService.getPlanModel(sub.getPlanId());
                    return SubscriptionBenefitsResponse.builder()
                            .hasFreeShipping(plan.isFreeShipping())
                            .discountPercentage(plan.getDiscountPercentage())
                            .planName(plan.getName())
                            .build();
                })
                .orElse(SubscriptionBenefitsResponse.builder()
                        .hasFreeShipping(false)
                        .discountPercentage(0)
                        .planName(null)
                        .build());
    }

    private UserSubscriptionResponse mapToResponse(UserSubscription subscription) {
        SubscriptionPlan plan = planService.getPlanModel(subscription.getPlanId());

        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUserId())
                .plan(com.dresstyle.subscriptionservice.dto.SubscriptionPlanResponse.builder()
                        .id(plan.getId())
                        .name(plan.getName())
                        .description(plan.getDescription())
                        .price(plan.getPrice())
                        .discountPercentage(plan.getDiscountPercentage())
                        .freeShipping(plan.isFreeShipping())
                        .isActive(plan.isActive())
                        .createdAt(plan.getCreatedAt())
                        .build())
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .renewalDate(subscription.getRenewalDate())
                .autoRenew(subscription.isAutoRenew())
                .build();
    }
}

