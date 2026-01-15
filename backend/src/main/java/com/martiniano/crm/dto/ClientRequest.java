package com.martiniano.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String company;
    private String primaryContactName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String phone;
    private String address;
    private String notes;
    private String tags;
    private String status;
}
