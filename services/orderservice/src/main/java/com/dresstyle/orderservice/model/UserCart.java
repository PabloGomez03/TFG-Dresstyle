package com.dresstyle.orderservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_carts")
public class UserCart {
    @Id
    private String id;

    private String userKey;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private Instant updatedAt;
}