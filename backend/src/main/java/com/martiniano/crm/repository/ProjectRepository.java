package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Legacy method for backward compatibility (clientId -> accountId)
    @Deprecated
    default List<Project> findByClientId(Long clientId) {
        return findByAccountId(clientId);
    }
    
    List<Project> findByAccountId(Long accountId);
    
    Page<Project> findByAccountId(Long accountId, Pageable pageable);
    
    List<Project> findByType(String type);
    
    Page<Project> findByType(String type, Pageable pageable);
    
    Page<Project> findByStatus(String status, Pageable pageable);
    
    long countByStatus(String status);
    
    long countByType(String type);
    
    Optional<Project> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Project> findByOwnerId(Long ownerId);
    
    Page<Project> findByOwnerId(Long ownerId, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.type = :type AND p.status = :status")
    List<Project> findByTypeAndStatus(@Param("type") String type, @Param("status") String status);
    
    @Query("SELECT p FROM Project p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Project> searchProjects(@Param("search") String search, Pageable pageable);
}

