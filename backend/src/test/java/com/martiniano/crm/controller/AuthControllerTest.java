package com.martiniano.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martiniano.crm.dto.LoginRequest;
import com.martiniano.crm.dto.LoginResponse;
import com.martiniano.crm.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void testLoginSuccess_WithEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        request.setPassword("admin123");

        LoginResponse response = new LoginResponse(
                "mock-jwt-token",
                "admin",
                "admin@martiniano.dev",
                "ADMIN"
        );

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@martiniano.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginSuccess_WithUsername() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = new LoginResponse(
                "mock-jwt-token",
                "admin",
                "admin@martiniano.dev",
                "ADMIN"
        );

        when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    void testLoginFailure_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        request.setPassword("wrongpassword");

        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid email/username or password"));
    }

    @Test
    void testLoginFailure_ValidationError_MissingEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPassword("admin123");
        // email/username is missing

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.username").exists());
    }

    @Test
    void testLoginFailure_ValidationError_MissingPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        // password is missing

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void testLoginFailure_ValidationError_EmptyFields() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
}
