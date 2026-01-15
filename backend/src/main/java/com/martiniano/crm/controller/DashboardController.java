package com.martiniano.crm.controller;

import com.martiniano.crm.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getDashboardKPIs() {
        Map<String, Object> kpis = dashboardService.getDashboardKPIs();
        return ResponseEntity.ok(kpis);
    }
}
