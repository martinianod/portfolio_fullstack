package com.martiniano.crm.repository;

import com.martiniano.crm.entity.GitHubRepo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for GitHubRepo entities.
 */
@Repository
public interface GitHubRepoRepository extends JpaRepository<GitHubRepo, Long> {
    
    Optional<GitHubRepo> findByRepoFullName(String repoFullName);
    
    boolean existsByRepoFullName(String repoFullName);
    
    List<GitHubRepo> findByStatus(String status);
    
    List<GitHubRepo> findByStatusOrderByCreatedAtDesc(String status);
}
