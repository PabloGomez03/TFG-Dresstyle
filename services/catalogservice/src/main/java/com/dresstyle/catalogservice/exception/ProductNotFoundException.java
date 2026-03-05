package com.dresstyle.catalogservice.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String productId) {
        super("No existe el producto con id: " + productId);
    }
}
