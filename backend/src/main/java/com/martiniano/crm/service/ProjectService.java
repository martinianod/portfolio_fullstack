package com.martiniano.crm.service;

import com.martiniano.crm.dto.ProjectRequest;
import com.martiniano.crm.entity.Project;
import com.martiniano.crm.repository.AccountRepository;
import com.martiniano.crm.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AccountRepository accountRepository;
    private final ActivityService activityService;

    public ProjectService(ProjectRepository projectRepository,
                          AccountRepository accountRepository,
                          ActivityService activityService) {
        this.projectRepository = projectRepository;
        this.accountRepository = accountRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        // For CLIENT type projects, accountId is required
        if ("CLIENT".equals(request.getType()) || request.getClientId() != null) {
            Long accountId = request.getAccountId() != null ? request.getAccountId() : request.getClientId();
            if (accountId == null) {
                throw new IllegalArgumentException("accountId is required for CLIENT projects");
            }
            if (!accountRepository.existsById(accountId)) {
                throw new EntityNotFoundException("Account not found with id: " + accountId);
            }
        }

        Project project = new Project();
        // Support both accountId and clientId (legacy) from request
        project.setAccountId(request.getAccountId() != null ? request.getAccountId() : request.getClientId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "PLANNED");
        project.setType(request.getType() != null ? request.getType() : "CLIENT");
        project.setStartDate(request.getStartDate());
        project.setTargetDate(request.getTargetDate());
        project.setCompletionDate(request.getCompletionDate());
        project.setStack(request.getStack());
        project.setDeployLink(request.getDeployLink());
        project.setEstimatedHours(request.getEstimatedHours());
        project.setActualHours(request.getActualHours());
        project.setBudgetAmount(request.getBudgetAmount());

        Project savedProject = projectRepository.save(project);

        activityService.logActivity("PROJECT", savedProject.getId(), "CREATED", 
                "Project created", null, null);

        return savedProject;
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Project> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByClientId(Long clientId) {
        // Legacy method - delegates to getProjectsByAccountId
        return getProjectsByAccountId(clientId);
    }

    @Transactional(readOnly = true)
    public List<Project> getProjectsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new EntityNotFoundException("Account not found with id: " + accountId);
        }
        return projectRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjectsByStatus(String status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequest request) {
        Project project = getProjectById(id);

        // Support both accountId and clientId (legacy) from request
        Long newAccountId = request.getAccountId() != null ? request.getAccountId() : request.getClientId();
        if (newAccountId != null && !newAccountId.equals(project.getAccountId())) {
            if (!accountRepository.existsById(newAccountId)) {
                throw new EntityNotFoundException("Account not found with id: " + newAccountId);
            }
            project.setAccountId(newAccountId);
        }
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getTargetDate() != null) {
            project.setTargetDate(request.getTargetDate());
        }
        if (request.getCompletionDate() != null) {
            project.setCompletionDate(request.getCompletionDate());
        }
        if (request.getStack() != null) {
            project.setStack(request.getStack());
        }
        if (request.getDeployLink() != null) {
            project.setDeployLink(request.getDeployLink());
        }
        if (request.getEstimatedHours() != null) {
            project.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            project.setActualHours(request.getActualHours());
        }
        if (request.getBudgetAmount() != null) {
            project.setBudgetAmount(request.getBudgetAmount());
        }

        Project updatedProject = projectRepository.save(project);

        activityService.logActivity("PROJECT", project.getId(), "UPDATED", 
                "Project information updated", null, null);

        return updatedProject;
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
        
        activityService.logActivity("PROJECT", id, "DELETED", "Project deleted", null, null);
    }

    @Transactional(readOnly = true)
    public long countProjectsByStatus(String status) {
        return projectRepository.countByStatus(status);
    }
}
