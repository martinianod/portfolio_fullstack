package com.martiniano.crm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeadResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String budgetRange;
    private String projectType;
    private String message;
    private String source;
    private String stage;
    private String priority;
    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
