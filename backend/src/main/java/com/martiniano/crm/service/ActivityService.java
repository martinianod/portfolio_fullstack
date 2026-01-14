package com.martiniano.crm.service;

import com.martiniano.crm.dto.ActivityRequest;
import com.martiniano.crm.entity.Activity;
import com.martiniano.crm.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional
    public Activity createActivity(ActivityRequest request) {
        Activity activity = new Activity();
        activity.setEntityType(request.getEntityType());
        activity.setEntityId(request.getEntityId());
        activity.setActivityType(request.getActivityType());
        activity.setDescription(request.getDescription());
        activity.setPayload(request.getPayload());

        return activityRepository.save(activity);
    }

    @Transactional
    public Activity logActivity(String entityType, Long entityId, String activityType, 
                                String description, Map<String, Object> payload, Long createdBy) {
        Activity activity = new Activity();
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setPayload(payload);
        activity.setCreatedBy(createdBy);

        return activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public Page<Activity> getActivityTimeline(String entityType, Long entityId, Pageable pageable) {
        return activityRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Activity> getActivityTimeline(String entityType, Long entityId, int page, int size) {
        return getActivityTimeline(entityType, entityId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Activity> getAllActivities(Pageable pageable) {
        return activityRepository.findAll(pageable);
    }
}
