package com.martiniano.crm.controller;

import com.martiniano.crm.dto.ActivityRequest;
import com.martiniano.crm.entity.Activity;
import com.martiniano.crm.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/activities")
@PreAuthorize("hasRole('ADMIN')")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Activity> createActivity(@Valid @RequestBody ActivityRequest request) {
        Activity activity = activityService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Page<Activity>> getActivitiesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Pageable pageable) {
        Page<Activity> activities = activityService.getActivityTimeline(entityType, entityId, pageable);
        return ResponseEntity.ok(activities);
    }
}
