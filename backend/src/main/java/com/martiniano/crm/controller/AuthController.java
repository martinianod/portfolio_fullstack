package com.martiniano.crm.controller;

import com.martiniano.crm.dto.LoginRequest;
import com.martiniano.crm.dto.LoginResponse;
import com.martiniano.crm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        // Log incoming request (without password for security)
        log.info("Login attempt - Path: {}, Method: {}, Content-Type: {}, Email: {}", 
                httpRequest.getRequestURI(),
                httpRequest.getMethod(),
                httpRequest.getHeader("Content-Type"),
                request.getEmail());
        
        LoginResponse response = authService.authenticate(request);
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
        // Exceptions are handled by GlobalExceptionHandler
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // This endpoint can be used to verify if the token is still valid
        return ResponseEntity.ok().body(Map.of("message", "Token is valid"));
    }
}
