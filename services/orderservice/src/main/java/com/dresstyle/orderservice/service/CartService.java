package com.dresstyle.orderservice.service;

import com.dresstyle.orderservice.dto.CartRequest;
import com.dresstyle.orderservice.dto.CartResponse;
import com.dresstyle.orderservice.model.CartItem;
import com.dresstyle.orderservice.model.UserCart;
import com.dresstyle.orderservice.repository.UserCartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserCartRepository userCartRepository;

    public CartResponse getCart(String userKey) {
        return userCartRepository.findByUserKey(userKey)
                .map(this::toResponse)
                .orElseGet(() -> CartResponse.builder()
                        .userKey(userKey)
                        .items(new ArrayList<>())
                        .updatedAt(null)
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
        return CartResponse.builder()
                .userKey(cart.getUserKey())
                .items(cart.getItems() == null ? new ArrayList<>() : cart.getItems())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}