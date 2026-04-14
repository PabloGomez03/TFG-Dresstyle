package com.dresstyle.orderservice.dto;

import com.dresstyle.orderservice.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String userKey;
    private List<CartItem> items;
    private Instant updatedAt;
}