package com.martiniano.crm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectRequest {
    // Support both accountId (new) and clientId (legacy) for backward compatibility
    private Long accountId;
    private Long clientId;  // Deprecated, use accountId

    @NotBlank(message = "Project name is required")
    private String name;

    private String code;
    private String description;
    private String status;
    private String type;  // CLIENT or INTERNAL
    private LocalDate startDate;
    private LocalDate targetDate;
    private LocalDate completionDate;
    private String stack;
    private String deployLink;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal budgetAmount;
}
