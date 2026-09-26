package com.linkedin.usersservice.controller;

import com.linkedin.usersservice.dto.AuthResponse;
import com.linkedin.usersservice.dto.LoginRequest;
import com.linkedin.usersservice.dto.RegisterRequest;
import com.linkedin.usersservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Received request to register user {}", registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(registerRequest));
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Received request to login user {}", loginRequest);
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
