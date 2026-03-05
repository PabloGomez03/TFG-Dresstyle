package com.dresstyle.catalogservice.service;

import com.dresstyle.catalogservice.dto.ProductRequest;
import com.dresstyle.catalogservice.dto.ProductResponse;
import com.dresstyle.catalogservice.exception.ProductNotFoundException;
import com.dresstyle.catalogservice.model.Product;
import com.dresstyle.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductResponse.from(product);
    }

    public ProductResponse create(ProductRequest request) {
        String normalizedName = request.getName().trim();

        if (productRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }

        Product product = Product.builder()
                .name(normalizedName)
                .description(trimToNull(request.getDescription()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    public ProductResponse update(String id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        String normalizedName = request.getName().trim();
        if (productRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }

        existingProduct.setName(normalizedName);
        existingProduct.setDescription(trimToNull(request.getDescription()));
        existingProduct.setImageUrl(trimToNull(request.getImageUrl()));
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStock(request.getStock());

        Product savedProduct = productRepository.save(existingProduct);
        return ProductResponse.from(savedProduct);
    }

    public void delete(String id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
