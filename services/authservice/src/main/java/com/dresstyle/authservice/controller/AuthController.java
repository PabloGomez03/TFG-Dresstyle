package com.dresstyle.authservice.controller;

import com.dresstyle.authservice.dto.AuthResponse;
import com.dresstyle.authservice.dto.LoginRequest;
import com.dresstyle.authservice.dto.RegisterRequest;
import com.dresstyle.authservice.dto.UpdateUserProfileRequest;
import com.dresstyle.authservice.dto.UserProfileResponse;
import com.dresstyle.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Usuario registrado con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateUserProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }
}