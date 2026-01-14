package com.martiniano.crm.service;

import com.martiniano.crm.repository.LeadRepository;
import com.martiniano.crm.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final LeadRepository leadRepository;
    private final ProjectRepository projectRepository;

    public DashboardService(LeadRepository leadRepository,
                            ProjectRepository projectRepository) {
        this.leadRepository = leadRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardKPIs() {
        Map<String, Object> kpis = new HashMap<>();

        Map<String, Long> leadCounts = getLeadCountsByStage();
        kpis.put("leadCounts", leadCounts);

        Map<String, Object> conversionMetrics = getConversionMetrics();
        kpis.put("conversionMetrics", conversionMetrics);

        long activeProjects = getActiveProjectsCount();
        kpis.put("activeProjects", activeProjects);

        Map<String, Long> projectCounts = getProjectCountsByStatus();
        kpis.put("projectCounts", projectCounts);

        return kpis;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLeadCountsByStage() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("NEW", leadRepository.countByStage("NEW"));
        counts.put("CONTACTED", leadRepository.countByStage("CONTACTED"));
        counts.put("QUALIFIED", leadRepository.countByStage("QUALIFIED"));
        counts.put("PROPOSAL", leadRepository.countByStage("PROPOSAL"));
        counts.put("NEGOTIATION", leadRepository.countByStage("NEGOTIATION"));
        counts.put("CONVERTED", leadRepository.countByStage("CONVERTED"));
        counts.put("LOST", leadRepository.countByStage("LOST"));
        return counts;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getConversionMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalLeads = leadRepository.count();
        long convertedLeads = leadRepository.countByStage("CONVERTED");
        long lostLeads = leadRepository.countByStage("LOST");

        double conversionRate = totalLeads > 0 ? (double) convertedLeads / totalLeads * 100 : 0.0;
        double lossRate = totalLeads > 0 ? (double) lostLeads / totalLeads * 100 : 0.0;

        metrics.put("totalLeads", totalLeads);
        metrics.put("convertedLeads", convertedLeads);
        metrics.put("lostLeads", lostLeads);
        metrics.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
        metrics.put("lossRate", Math.round(lossRate * 100.0) / 100.0);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentLeads = leadRepository.countByCreatedAtAfter(thirtyDaysAgo);
        metrics.put("recentLeads", recentLeads);

        return metrics;
    }

    @Transactional(readOnly = true)
    public long getActiveProjectsCount() {
        return projectRepository.countByStatus("IN_PROGRESS") + 
               projectRepository.countByStatus("DISCOVERY") +
               projectRepository.countByStatus("DEVELOPMENT");
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getProjectCountsByStatus() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("DISCOVERY", projectRepository.countByStatus("DISCOVERY"));
        counts.put("DEVELOPMENT", projectRepository.countByStatus("DEVELOPMENT"));
        counts.put("IN_PROGRESS", projectRepository.countByStatus("IN_PROGRESS"));
        counts.put("TESTING", projectRepository.countByStatus("TESTING"));
        counts.put("DEPLOYED", projectRepository.countByStatus("DEPLOYED"));
        counts.put("COMPLETED", projectRepository.countByStatus("COMPLETED"));
        counts.put("ON_HOLD", projectRepository.countByStatus("ON_HOLD"));
        counts.put("CANCELLED", projectRepository.countByStatus("CANCELLED"));
        return counts;
    }
}
