package com.martiniano.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
    private String status;
    private LocalDate startDate;
    private LocalDate targetDate;
    private LocalDate completionDate;
    private String stack;
    private String repoLink;
    private String deployLink;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private BigDecimal budgetAmount;
}
