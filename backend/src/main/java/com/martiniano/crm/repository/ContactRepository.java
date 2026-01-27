package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Contact entities.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByAccountId(Long accountId);
    
    Optional<Contact> findByAccountIdAndIsPrimaryTrue(Long accountId);
    
    boolean existsByAccountIdAndEmail(Long accountId, String email);
    
    long countByAccountId(Long accountId);
    
    @Modifying
    @Query("UPDATE Contact c SET c.isPrimary = false WHERE c.accountId = :accountId AND c.id != :contactId")
    void unsetPrimaryForAccount(@Param("accountId") Long accountId, @Param("contactId") Long contactId);
    
    @Modifying
    @Query("UPDATE Contact c SET c.isPrimary = false WHERE c.accountId = :accountId")
    void unsetAllPrimaryForAccount(@Param("accountId") Long accountId);
}
