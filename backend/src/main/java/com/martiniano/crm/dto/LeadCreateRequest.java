package com.martiniano.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LeadCreateRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String company;

    @Size(max = 100)
    private String budgetRange;

    @Size(max = 100)
    private String projectType;

    @NotBlank(message = "Message is required")
    @Size(min = 10, message = "Message must be at least 10 characters")
    private String message;

    private String source = "web";
}
