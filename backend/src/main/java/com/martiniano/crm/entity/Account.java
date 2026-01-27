package com.martiniano.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Account entity (evolved from Client).
 * Represents a company or client account in the CRM system.
 * Supports multiple contacts and custom fields for extensibility.
 */
@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String company;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE";

    private String industry;

    private String website;

    private String tags;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private Map<String, Object> customFields = new HashMap<>();

    @Column(name = "created_from_lead_id")
    private Long createdFromLeadId;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (slug == null && name != null) {
            slug = generateSlug(name);
        }
        if (customFields == null) {
            customFields = new HashMap<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Generate a URL-friendly slug from a name.
     * Example: "ACME Corporation" -> "acme-corporation"
     */
    public static String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
