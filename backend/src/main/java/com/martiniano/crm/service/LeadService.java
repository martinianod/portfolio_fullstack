package com.martiniano.crm.service;

import com.martiniano.crm.dto.LeadCreateRequest;
import com.martiniano.crm.dto.LeadUpdateRequest;
import com.martiniano.crm.entity.Lead;
import com.martiniano.crm.entity.User;
import com.martiniano.crm.repository.LeadRepository;
import com.martiniano.crm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final EmailService emailService;

    public LeadService(LeadRepository leadRepository,
                       UserRepository userRepository,
                       ActivityService activityService,
                       EmailService emailService) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.emailService = emailService;
    }

    @Transactional
    public Lead createLead(LeadCreateRequest request) {
        Lead lead = new Lead();
        lead.setName(request.getName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setCompany(request.getCompany());
        lead.setBudgetRange(request.getBudgetRange());
        lead.setProjectType(request.getProjectType());
        lead.setMessage(request.getMessage());
        lead.setSource(request.getSource());
        lead.setStage("NEW");
        lead.setPriority("MEDIUM");

        Lead savedLead = leadRepository.save(lead);

        activityService.logActivity("LEAD", savedLead.getId(), "CREATED", 
                "Lead created from " + request.getSource(), null, null);
        
        // Send notification email
        emailService.sendContactEmail(request.getName(), request.getEmail(), request.getMessage());

        return savedLead;
    }

    @Transactional(readOnly = true)
    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Lead> getAllLeads(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Lead> searchLeads(String search, Pageable pageable) {
        return leadRepository.searchLeads(search, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Lead> getLeadsByStage(String stage, Pageable pageable) {
        return leadRepository.findByStage(stage, pageable);
    }

    @Transactional
    public Lead updateLead(Long id, LeadUpdateRequest request) {
        Lead lead = getLeadById(id);
        String oldStage = lead.getStage();

        if (request.getName() != null) {
            lead.setName(request.getName());
        }
        if (request.getEmail() != null) {
            lead.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            lead.setPhone(request.getPhone());
        }
        if (request.getCompany() != null) {
            lead.setCompany(request.getCompany());
        }
        if (request.getBudgetRange() != null) {
            lead.setBudgetRange(request.getBudgetRange());
        }
        if (request.getProjectType() != null) {
            lead.setProjectType(request.getProjectType());
        }
        if (request.getMessage() != null) {
            lead.setMessage(request.getMessage());
        }
        if (request.getPriority() != null) {
            lead.setPriority(request.getPriority());
        }
        if (request.getAssignedTo() != null) {
            User user = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            lead.setAssignedTo(user);
        }
        if (request.getStage() != null && !request.getStage().equals(oldStage)) {
            lead.setStage(request.getStage());
            updateStageWithLogging(lead, oldStage, request.getStage());
        }

        return leadRepository.save(lead);
    }

    @Transactional
    public Lead updateLeadStage(Long id, String newStage) {
        Lead lead = getLeadById(id);
        String oldStage = lead.getStage();
        
        if (!oldStage.equals(newStage)) {
            lead.setStage(newStage);
            updateStageWithLogging(lead, oldStage, newStage);
        }
        
        return leadRepository.save(lead);
    }

    private void updateStageWithLogging(Lead lead, String oldStage, String newStage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("oldStage", oldStage);
        payload.put("newStage", newStage);

        String description = String.format("Lead stage changed from %s to %s", oldStage, newStage);
        activityService.logActivity("LEAD", lead.getId(), "STAGE_CHANGED", description, payload, null);
    }

    @Transactional
    public void deleteLead(Long id) {
        Lead lead = getLeadById(id);
        leadRepository.delete(lead);
        
        activityService.logActivity("LEAD", id, "DELETED", "Lead deleted", null, null);
    }

    @Transactional(readOnly = true)
    public long countLeadsByStage(String stage) {
        return leadRepository.countByStage(stage);
    }
}
