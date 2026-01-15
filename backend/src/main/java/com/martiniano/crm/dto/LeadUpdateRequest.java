package com.martiniano.crm.dto;

import lombok.Data;

@Data
public class LeadUpdateRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String budgetRange;
    private String projectType;
    private String message;
    private String stage;
    private String priority;
    private Long assignedTo;
}
