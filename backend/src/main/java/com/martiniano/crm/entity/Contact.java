package com.martiniano.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Contact entity.
 * Represents an individual contact person associated with an Account.
 * Each account can have multiple contacts, with one marked as primary.
 */
@Data
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "account_id")
    private Long accountId;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String position;

    @Column(nullable = false, name = "is_primary")
    private Boolean isPrimary = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isPrimary == null) {
            isPrimary = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Get the full name of the contact.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
