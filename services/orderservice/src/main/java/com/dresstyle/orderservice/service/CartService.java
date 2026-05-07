package com.dresstyle.orderservice.service;

import com.dresstyle.orderservice.dto.CartRequest;
import com.dresstyle.orderservice.dto.CartResponse;
import com.dresstyle.orderservice.model.CartItem;
import com.dresstyle.orderservice.model.UserCart;
import com.dresstyle.orderservice.repository.UserCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final double SHIPPING_0_TO_30 = 5.0;
    private static final double SHIPPING_30_TO_60 = 3.0;
    private static final double SHIPPING_60_TO_90 = 1.5;
    private static final double FREE_SHIPPING_THRESHOLD = 90.0;

    private final UserCartRepository userCartRepository;

    public CartResponse getCart(String userKey) {
        return userCartRepository.findByUserKey(userKey)
                .map(this::toResponse)
                .orElseGet(() -> CartResponse.builder()
                        .userKey(userKey)
                        .items(new ArrayList<>())
                        .updatedAt(null)
                .subtotal(0.0)
                .shippingCost(0.0)
                .total(0.0)
                        .build());
    }

    public CartResponse saveCart(String userKey, CartRequest request) {
        List<CartItem> items = normalizeItems(request == null ? null : request.getItems());
        UserCart cart = userCartRepository.findByUserKey(userKey)
                .orElseGet(() -> UserCart.builder().userKey(userKey).build());

        cart.setUserKey(userKey);
        cart.setItems(items);
        cart.setUpdatedAt(Instant.now());

        return toResponse(userCartRepository.save(cart));
    }

    public void clearCart(String userKey) {
        userCartRepository.deleteByUserKey(userKey);
    }

    private List<CartItem> normalizeItems(List<CartItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }

        List<CartItem> normalized = new ArrayList<>();
        for (CartItem item : items) {
            if (item == null) {
                continue;
            }

            normalized.add(CartItem.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .imageUrl(item.getImageUrl())
                    .quantity(Math.max(1, item.getQuantity()))
                    .size(item.getSize())
                    .build());
        }

        return normalized;
    }

    private CartResponse toResponse(UserCart cart) {
        List<CartItem> items = cart.getItems() == null ? new ArrayList<>() : cart.getItems();
        double subtotal = calculateSubtotal(items);
        double shippingCost = calculateShippingCost(subtotal);

        return CartResponse.builder()
                .userKey(cart.getUserKey())
                .items(items)
                .updatedAt(cart.getUpdatedAt())
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .total(roundToTwoDecimals(subtotal + shippingCost))
                .build();
    }

    private double calculateSubtotal(List<CartItem> items) {
        double subtotal = 0.0;

        for (CartItem item : items) {
            if (item == null) {
                continue;
            }

            subtotal += item.getPrice() * item.getQuantity();
        }

        return roundToTwoDecimals(subtotal);
    }

    private double calculateShippingCost(double subtotal) {
        if (subtotal <= 0.0) {
            return 0.0;
        }

        if (subtotal < 30.0) {
            return SHIPPING_0_TO_30;
        }

        if (subtotal < 60.0) {
            return SHIPPING_30_TO_60;
        }

        if (subtotal < FREE_SHIPPING_THRESHOLD) {
            return SHIPPING_60_TO_90;
        }

        return 0.0;
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}