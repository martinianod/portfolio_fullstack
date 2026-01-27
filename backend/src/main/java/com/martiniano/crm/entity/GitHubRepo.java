package com.martiniano.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GitHub Repository entity.
 * Stores metadata about GitHub repositories associated with projects.
 */
@Data
@Entity
@Table(name = "github_repos")
public class GitHubRepo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "repo_full_name")
    private String repoFullName;

    @Column(nullable = false, name = "repo_url")
    private String repoUrl;

    @Column(name = "default_branch")
    private String defaultBranch = "main";

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "TEXT", name = "provisioning_error")
    private String provisioningError;

    @Column(name = "provisioned_at")
    private LocalDateTime provisionedAt;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        if (defaultBranch == null) {
            defaultBranch = "main";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
