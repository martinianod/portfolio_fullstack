package com.martiniano.crm.controller;

import com.martiniano.crm.dto.LoginRequest;
import com.martiniano.crm.dto.LoginResponse;
import com.martiniano.crm.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
        
        try {
            LoginResponse response = authService.authenticate(request);
            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {} - Reason: {}", request.getEmail(), e.getMessage());
            throw e; // Let the global exception handler deal with it
        } catch (Exception e) {
            log.error("Unexpected error during login for email: {} - Error: {}", 
                    request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // This endpoint can be used to verify if the token is still valid
        return ResponseEntity.ok().body(Map.of("message", "Token is valid"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed");
        error.put("message", "Invalid email or password");
        log.debug("Returning 401 - Authentication failed");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("error", "Validation failed");
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            log.warn("Validation error - Field: {}, Message: {}", fieldName, errorMessage);
        });
        errors.put("fields", fieldErrors);
        log.debug("Returning 400 - Validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
