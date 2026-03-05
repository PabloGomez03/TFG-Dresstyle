package com.dresstyle.catalogservice.dto;

import com.dresstyle.catalogservice.model.Product;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {
    String id;
    String name;
    String description;
    String imageUrl;
    double price;
    int stock;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}
