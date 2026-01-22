package com.martiniano.crm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.martiniano.crm.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for login functionality
 * Tests the complete flow from request to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginWithEmail_Success() throws Exception {
        // Test login with email (standard way)
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@martiniano.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginWithUsername_Success() throws Exception {
        // Test login with username (backward compatibility)
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@martiniano.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testLoginWithEmail_InvalidPassword() throws Exception {
        // Test login with wrong password
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid email/username or password"));
    }

    @Test
    void testLoginWithUsername_InvalidPassword() throws Exception {
        // Test login with username and wrong password
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid email/username or password"));
    }

    @Test
    void testLogin_MissingBothEmailAndUsername() throws Exception {
        // Test validation - neither email nor username provided
        LoginRequest request = new LoginRequest();
        request.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    void testLogin_MissingPassword() throws Exception {
        // Test validation - password missing
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void testLogin_EmailTakesPrecedenceOverUsername() throws Exception {
        // Test that when both email and username are provided, email takes precedence
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@martiniano.dev");
        request.setUsername("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@martiniano.dev"));
    }

    @Test
    void testLogin_NonExistentEmail() throws Exception {
        // Test login with non-existent email
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("somepassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

    @Test
    void testLogin_NonExistentUsername() throws Exception {
        // Test login with non-existent username
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("somepassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"));
    }
}
