package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Account entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findBySlug(String slug);
    
    Optional<Account> findByEmail(String email);
    
    boolean existsBySlug(String slug);
    
    boolean existsByCreatedFromLeadId(Long leadId);
    
    List<Account> findByStatus(String status);
    
    Page<Account> findByStatus(String status, Pageable pageable);
    
    List<Account> findByOwnerId(Long ownerId);
    
    Page<Account> findByOwnerId(Long ownerId, Pageable pageable);
    
    @Query("SELECT a FROM Account a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.company) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.slug) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Account> searchAccounts(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT a FROM Account a WHERE a.industry = :industry")
    List<Account> findByIndustry(@Param("industry") String industry);
}
