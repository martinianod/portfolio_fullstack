package com.martiniano.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email or username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Alias for username to support email field from frontend
    public void setEmail(String email) {
        this.username = email;
    }
    
    public String getEmail() {
        return this.username;
    }
}
