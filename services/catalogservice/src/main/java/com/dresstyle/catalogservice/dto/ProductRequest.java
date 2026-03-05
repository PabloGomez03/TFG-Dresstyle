package com.dresstyle.catalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    private String imageUrl;

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private double price;

    @Min(value = 0, message = "El stock debe ser mayor o igual a 0")
    private int stock;
}
