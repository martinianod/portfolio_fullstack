package com.martiniano.crm.repository;

import com.martiniano.crm.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByMilestoneId(Long milestoneId);
    List<Task> findByProjectIdAndStatus(Long projectId, String status);
}
