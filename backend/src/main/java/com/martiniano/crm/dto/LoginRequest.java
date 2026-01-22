package com.martiniano.crm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("username")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Custom validation to ensure at least one of email or username is provided
     */
    @AssertTrue(message = "Either email or username must be provided")
    private boolean isEitherEmailOrUsernameProvided() {
        return (email != null && !email.trim().isEmpty()) || 
               (username != null && !username.trim().isEmpty());
    }

    /**
     * Get the principal (email or username) for authentication
     */
    public String getPrincipal() {
        // Prefer email over username if both are provided
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return username;
    }
}
