package com.martiniano.crm.controller;

import com.martiniano.crm.dto.ProjectRequest;
import com.martiniano.crm.entity.Project;
import com.martiniano.crm.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@PreAuthorize("hasRole('ADMIN')")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        Project project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @GetMapping
    public ResponseEntity<Page<Project>> getAllProjects(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        
        Page<Project> projects;
        if (status != null && !status.isBlank()) {
            projects = projectService.getProjectsByStatus(status, pageable);
        } else {
            projects = projectService.getAllProjects(pageable);
        }
        
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Project>> getProjectsByClient(@PathVariable Long clientId) {
        List<Project> projects = projectService.getProjectsByClientId(clientId);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        Project project = projectService.updateProject(id, request);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
