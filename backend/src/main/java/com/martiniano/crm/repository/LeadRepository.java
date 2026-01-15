package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    Page<Lead> findByStage(String stage, Pageable pageable);
    Page<Lead> findBySource(String source, Pageable pageable);
    
    @Query("SELECT l FROM Lead l WHERE l.stage = :stage AND l.createdAt >= :since")
    List<Lead> findByStageAndCreatedAtAfter(@Param("stage") String stage, @Param("since") LocalDateTime since);
    
    @Query("SELECT l FROM Lead l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(l.company) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> searchLeads(@Param("search") String search, Pageable pageable);
    
    long countByStage(String stage);
    long countByCreatedAtAfter(LocalDateTime since);
}
