package com.dresstyle.subscriptionservice.service;

import com.dresstyle.subscriptionservice.dto.SubscriptionPlanResponse;
import com.dresstyle.subscriptionservice.model.SubscriptionPlan;
import com.dresstyle.subscriptionservice.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;

    /**
     * Obtener todos los planes activos
     */
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        return planRepository.findAll()
                .stream()
                .filter(SubscriptionPlan::isActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un plan por ID
     */
    public SubscriptionPlanResponse getPlanById(String planId) {
        return planRepository.findById(planId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));
    }

    /**
     * Obtener un plan por nombre
     */
    public SubscriptionPlanResponse getPlanByName(String name) {
        return planRepository.findByName(name)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + name));
    }

    /**
     * Obtener el modelo del plan (para uso interno)
     */
    public SubscriptionPlan getPlanModel(String planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado: " + planId));
    }

    /**
     * Inicializar planes si no existen
     */
    public void initializePlans() {
        if (planRepository.count() == 0) {
            SubscriptionPlan basic = SubscriptionPlan.builder()
                    .name("Basic")
                    .description("Envío gratis en toda tu tienda de moda")
                    .price(4.99)
                    .discountPercentage(0)
                    .freeShipping(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            SubscriptionPlan premium = SubscriptionPlan.builder()
                    .name("Premium")
                    .description("5% descuento + envío gratis en toda la tienda")
                    .price(9.99)
                    .discountPercentage(5)
                    .freeShipping(true)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            planRepository.save(basic);
            planRepository.save(premium);
        }
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .discountPercentage(plan.getDiscountPercentage())
                .freeShipping(plan.isFreeShipping())
                .isActive(plan.isActive())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
