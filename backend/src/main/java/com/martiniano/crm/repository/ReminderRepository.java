package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Reminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    Page<Reminder> findByEntityTypeAndEntityIdOrderByDueAtAsc(String entityType, Long entityId, Pageable pageable);
    
    @Query("SELECT r FROM Reminder r WHERE r.status = 'PENDING' AND r.dueAt <= :now ORDER BY r.dueAt ASC")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);
}
