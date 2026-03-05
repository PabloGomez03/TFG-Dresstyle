package com.dresstyle.authservice.service;


import com.dresstyle.authservice.dto.AuthResponse;
import com.dresstyle.authservice.dto.LoginRequest;
import com.dresstyle.authservice.dto.RegisterRequest;
import com.dresstyle.authservice.dto.UserRegisteredEvent;
import com.dresstyle.authservice.exception.ExistingEmailException;
import com.dresstyle.authservice.repository.UserRepository;
import com.dresstyle.authservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // Inyectar el servicio de tokens
    private final RabbitTemplate rabbitTemplate;

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Generar el token JWT
        String token = jwtService.generateToken(user);

        // Devolver la respuesta oficial
        return AuthResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public void register(RegisterRequest request) {
        //Validar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ExistingEmailException("Ya existe un usuario con el mismo email!");
        }

        // Mapear DTO a User y cifrar contraseña
        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Collections.singletonList("ROLE_CLIENT")))
                .activeSubscription(false)
                .build();

        // 3. Persistir en MongoDB
        userRepository.save(newUser);

        // Crear el evento
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email(newUser.getEmail())
                .nombre(newUser.getName())
                .userId(newUser.getUserId())
                .build();

        // Publicar el objeto (Spring lo convertirá a JSON automáticamente)
        rabbitTemplate.convertAndSend("notificationExchange", "registrationRoutingKey", event);

    }
}