package com.martiniano.crm.controller;

import com.martiniano.crm.dto.LeadCreateRequest;
import com.martiniano.crm.dto.LeadUpdateRequest;
import com.martiniano.crm.dto.LeadResponse;
import com.martiniano.crm.entity.Lead;
import com.martiniano.crm.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping("/public")
    public ResponseEntity<Map<String, Object>> createLeadPublic(@Valid @RequestBody LeadCreateRequest request) {
        Lead lead = leadService.createLead(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Lead created successfully");
        response.put("leadId", lead.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Lead>> getAllLeads(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String stage,
            Pageable pageable) {
        
        Page<Lead> leads;
        if (search != null && !search.isBlank()) {
            leads = leadService.searchLeads(search, pageable);
        } else if (stage != null && !stage.isBlank()) {
            leads = leadService.getLeadsByStage(stage, pageable);
        } else {
            leads = leadService.getAllLeads(pageable);
        }
        
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Lead> getLeadById(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Lead> updateLead(@PathVariable Long id, @Valid @RequestBody LeadUpdateRequest request) {
        Lead lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(lead);
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Lead> updateLeadStage(@PathVariable Long id, @RequestParam String stage) {
        Lead lead = leadService.updateLeadStage(id, stage);
        return ResponseEntity.ok(lead);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getLeadStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("NEW", leadService.countLeadsByStage("NEW"));
        stats.put("CONTACTED", leadService.countLeadsByStage("CONTACTED"));
        stats.put("QUALIFIED", leadService.countLeadsByStage("QUALIFIED"));
        stats.put("PROPOSAL", leadService.countLeadsByStage("PROPOSAL"));
        stats.put("NEGOTIATION", leadService.countLeadsByStage("NEGOTIATION"));
        stats.put("WON", leadService.countLeadsByStage("WON"));
        stats.put("LOST", leadService.countLeadsByStage("LOST"));
        return ResponseEntity.ok(stats);
    }
}
