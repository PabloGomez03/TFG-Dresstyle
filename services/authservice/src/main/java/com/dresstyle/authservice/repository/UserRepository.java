package com.dresstyle.authservice.repository;

import com.dresstyle.authservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    // Método necesario para el proceso de Login
    Optional<User> findByEmail(String email);

    // Método para validar si el email ya existe en el registro
    Boolean existsByEmail(String email);
}