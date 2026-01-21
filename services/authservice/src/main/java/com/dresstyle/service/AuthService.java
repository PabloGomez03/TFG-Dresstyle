package com.dresstyle.service;


import com.dresstyle.dto.RegisterRequest;
import com.dresstyle.model.User;
import com.dresstyle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registrar(RegisterRequest request) {
        //Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Mapear DTO a User y cifrar contraseña
        User nuevoUsuario = User.builder()
                .name(request.getNombre())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Collections.singletonList("ROLE_CLIENTE")))
                .suscripcionActiva(false)
                .build();

        // 3. Persistir en MongoDB
        userRepository.save(nuevoUsuario);
    }
}