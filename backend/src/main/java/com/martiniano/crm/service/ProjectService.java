package com.martiniano.crm.service;

import com.martiniano.crm.dto.ProjectRequest;
import com.martiniano.crm.entity.Project;
import com.martiniano.crm.repository.ClientRepository;
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
    private final ClientRepository clientRepository;
    private final ActivityService activityService;

    public ProjectService(ProjectRepository projectRepository,
                          ClientRepository clientRepository,
                          ActivityService activityService) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.activityService = activityService;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        if (!clientRepository.existsById(request.getClientId())) {
            throw new EntityNotFoundException("Client not found with id: " + request.getClientId());
        }

        Project project = new Project();
        project.setClientId(request.getClientId());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : "DISCOVERY");
        project.setStartDate(request.getStartDate());
        project.setTargetDate(request.getTargetDate());
        project.setCompletionDate(request.getCompletionDate());
        project.setStack(request.getStack());
        project.setRepoLink(request.getRepoLink());
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
        if (!clientRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Client not found with id: " + clientId);
        }
        return projectRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjectsByStatus(String status, Pageable pageable) {
        return projectRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Project updateProject(Long id, ProjectRequest request) {
        Project project = getProjectById(id);

        if (request.getClientId() != null && !request.getClientId().equals(project.getClientId())) {
            if (!clientRepository.existsById(request.getClientId())) {
                throw new EntityNotFoundException("Client not found with id: " + request.getClientId());
            }
            project.setClientId(request.getClientId());
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
        if (request.getRepoLink() != null) {
            project.setRepoLink(request.getRepoLink());
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
