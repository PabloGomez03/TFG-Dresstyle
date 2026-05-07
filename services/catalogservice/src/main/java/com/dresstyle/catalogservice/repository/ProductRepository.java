package com.dresstyle.catalogservice.repository;

import com.dresstyle.catalogservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface ProductRepository extends MongoRepository<Product, String> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    @Query("{ '$text': { '$search': ?0 } }")
    Page<Product> searchByTerm(String term, Pageable pageable);
}
