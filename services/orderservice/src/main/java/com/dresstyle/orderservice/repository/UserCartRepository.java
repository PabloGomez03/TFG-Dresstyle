package com.dresstyle.orderservice.repository;

import com.dresstyle.orderservice.model.UserCart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserCartRepository extends MongoRepository<UserCart, String> {
    Optional<UserCart> findByUserKey(String userKey);

    void deleteByUserKey(String userKey);
}