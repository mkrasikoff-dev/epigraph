package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.dto.AuthRequest;
import com.mkrasikoff.epigraph.dto.AuthResponse;
import com.mkrasikoff.epigraph.dto.ErrorResponse;
import com.mkrasikoff.epigraph.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        String token = authService.register(request.getEmail(), request.getPassword());

        log.info("New user registered — email = {}", request.getEmail());

        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());

        if (token == null) {
            log.info("Login failed — email = {}", request.getEmail());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Неверный email или пароль"));
        }

        log.info("User logged in — email = {}", request.getEmail());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
