package com.dresstyle.authservice.service;


import com.dresstyle.authservice.dto.AuthResponse;
import com.dresstyle.authservice.dto.LoginRequest;
import com.dresstyle.authservice.dto.ProfileAddressDto;
import com.dresstyle.authservice.dto.RegisterRequest;
import com.dresstyle.authservice.dto.UpdateUserProfileRequest;
import com.dresstyle.authservice.dto.UserProfileResponse;
import com.dresstyle.authservice.dto.UserRegisteredEvent;
import com.dresstyle.authservice.exception.ExistingEmailException;
import com.dresstyle.authservice.repository.UserRepository;
import com.dresstyle.authservice.model.Address;
import com.dresstyle.authservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.AmqpException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int MAX_ADDRESSES_PER_USER = 5;

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

        // Si falla la notificacion no bloqueamos el registro del usuario ya persistido.
        try {
            rabbitTemplate.convertAndSend("notificationExchange", "registrationRoutingKey", event);
        } catch (AmqpException ex) {
            log.warn("Usuario registrado pero no se pudo publicar evento de registro para {}", newUser.getEmail(), ex);
        }

    }

    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String[] fullName = splitName(user.getName());
        List<ProfileAddressDto> mappedAddresses = mapAddressesToDto(user.getAddresses());
        ProfileAddressDto primaryAddress = mappedAddresses.isEmpty() ? null : mappedAddresses.get(0);

        return UserProfileResponse.builder()
                .firstName(fullName[0])
                .lastName(fullName[1])
                .email(user.getEmail())
                .phone(user.getPhone())
            .address(primaryAddress != null ? primaryAddress.getStreet() : null)
            .city(primaryAddress != null ? primaryAddress.getCity() : null)
            .zipCode(primaryAddress != null ? primaryAddress.getZipCode() : null)
            .country(primaryAddress != null ? primaryAddress.getCountry() : null)
            .addresses(mappedAddresses)
                .build();
    }

    public UserProfileResponse updateProfile(String email, UpdateUserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(buildFullName(request.getFirstName(), request.getLastName()));
        user.setPhone(request.getPhone());

        user.setAddresses(resolveRequestedAddresses(request));

        userRepository.save(user);
        return getProfile(email);
    }

    private List<Address> resolveRequestedAddresses(UpdateUserProfileRequest request) {
        if (request.getAddresses() != null) {
            return mapDtoToAddresses(request.getAddresses());
        }

        if (!hasLegacyAddressData(request)) {
            return new ArrayList<>();
        }

        ProfileAddressDto legacyAddress = ProfileAddressDto.builder()
                .street(request.getAddress())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();

        List<ProfileAddressDto> legacyAddresses = new ArrayList<>();
        legacyAddresses.add(legacyAddress);
        return mapDtoToAddresses(legacyAddresses);
    }

    private boolean hasLegacyAddressData(UpdateUserProfileRequest request) {
        return notBlank(request.getAddress())
                || notBlank(request.getCity())
                || notBlank(request.getZipCode())
                || notBlank(request.getCountry());
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private List<ProfileAddressDto> mapAddressesToDto(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return new ArrayList<>();
        }

        return addresses.stream()
                .limit(MAX_ADDRESSES_PER_USER)
                .map(address -> ProfileAddressDto.builder()
                        .street(address.getStreet())
                        .city(address.getCity())
                        .zipCode(address.getZipCode())
                        .country(address.getCountry())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Address> mapDtoToAddresses(List<ProfileAddressDto> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return new ArrayList<>();
        }

        return addresses.stream()
                .limit(MAX_ADDRESSES_PER_USER)
                .map(address -> Address.builder()
                        .street(address.getStreet())
                        .city(address.getCity())
                        .zipCode(address.getZipCode())
                        .country(address.getCountry())
                        .build())
                .collect(Collectors.toList());
    }

    private String buildFullName(String firstName, String lastName) {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();
        return (safeFirstName + " " + safeLastName).trim();
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[] {"", ""};
        }

        String trimmed = fullName.trim();
        int firstSpaceIndex = trimmed.indexOf(' ');

        if (firstSpaceIndex < 0) {
            return new String[] {trimmed, ""};
        }

        String firstName = trimmed.substring(0, firstSpaceIndex).trim();
        String lastName = trimmed.substring(firstSpaceIndex + 1).trim();
        return new String[] {firstName, lastName};
    }
}