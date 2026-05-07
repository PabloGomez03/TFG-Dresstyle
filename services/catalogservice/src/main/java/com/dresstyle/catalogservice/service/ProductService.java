package com.dresstyle.catalogservice.service;

import com.dresstyle.catalogservice.dto.ProductRequest;
import com.dresstyle.catalogservice.dto.ProductResponse;
import com.dresstyle.catalogservice.dto.PagedProductResponse;
import com.dresstyle.catalogservice.exception.ProductNotFoundException;
import com.dresstyle.catalogservice.model.Product;
import com.dresstyle.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    public PagedProductResponse searchProducts(String query, Integer page, Integer size) {
        int pageNum = page == null || page < 0 ? 0 : page;
        int pageSize = size == null ? DEFAULT_PAGE_SIZE : Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.ASC, "name"));

        Page<Product> productPage;
        if (query == null || query.trim().isEmpty()) {
            productPage = productRepository.findAll(pageable);
        } else {
            productPage = productRepository.searchByTerm(query.trim(), pageable);
        }

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(ProductResponse::from)
                .toList();

        return PagedProductResponse.builder()
                .content(content)
                .totalElements((int) productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(pageNum)
                .pageSize(pageSize)
                .hasNext(productPage.hasNext())
                .hasPrevious(productPage.hasPrevious())
                .build();
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
            .category(trimToNull(request.getCategory()))
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
        existingProduct.setCategory(trimToNull(request.getCategory()));

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
