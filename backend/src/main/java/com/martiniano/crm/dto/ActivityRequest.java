package com.martiniano.crm.dto;

import lombok.Data;
import java.util.Map;
import java.time.LocalDateTime;

@Data
public class ActivityRequest {
    private String entityType;
    private Long entityId;
    private String activityType;
    private String description;
    private Map<String, Object> payload;
}
