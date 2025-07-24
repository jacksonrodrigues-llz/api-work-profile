package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final BoardColumnService boardColumnService;

    public Map<String, Object> getKanbanData(User user) {
        var activities = activityRepository.findByUserOrderByCreatedAtDesc(user);
        var columns = boardColumnService.getUserColumns(user);
        
        var activitiesByStatus = activities.stream()
            .collect(Collectors.groupingBy(Activity::getStatus));
        
        var result = new java.util.HashMap<String, Object>();
        result.put("activities", activities);
        result.put("columns", columns);
        
        // Manter compatibilidade com template atual
        for (var column : columns) {
            var columnActivities = activitiesByStatus.getOrDefault(column.getStatus(), java.util.List.of());
            var statusKey = column.getStatus().toLowerCase() + "Activities";
            var countKey = column.getStatus().toLowerCase() + "Count";
            
            // Mapear status específicos para compatibilidade
            switch (column.getStatus()) {
                case "TODO" -> {
                    result.put("todoActivities", columnActivities);
                    result.put("todoCount", columnActivities.size());
                }
                case "IN_PROGRESS" -> {
                    result.put("inProgressActivities", columnActivities);
                    result.put("inProgressCount", columnActivities.size());
                }
                case "DEV" -> {
                    result.put("devActivities", columnActivities);
                    result.put("devCount", columnActivities.size());
                }
                case "UAT" -> {
                    result.put("uatActivities", columnActivities);
                    result.put("uatCount", columnActivities.size());
                }
                case "HML" -> {
                    result.put("hmlActivities", columnActivities);
                    result.put("hmlCount", columnActivities.size());
                }
                case "PRD" -> {
                    result.put("prdActivities", columnActivities);
                    result.put("prdCount", columnActivities.size());
                }
                case "DEPLOY" -> {
                    result.put("deployActivities", columnActivities);
                    result.put("deployCount", columnActivities.size());
                }
                case "DONE" -> {
                    result.put("doneActivities", columnActivities);
                    result.put("doneCount", columnActivities.size());
                }
            }
        }
        
        return result;
    }

    public Activity saveActivity(Activity activity, User user) {
        var activityToSave = activity.toBuilder()
            .user(user)
            .build();
        
        if ("DONE".equals(activityToSave.getStatus()) && 
            activityToSave.getCompletedAt() == null) {
            activityToSave = activityToSave.toBuilder()
                .completedAt(LocalDateTime.now())
                .build();
        }
        
        var saved = activityRepository.save(activityToSave);
        log.info("[ACTIVITY] Criada: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    public Activity getActivityForEdit(Long id, User user) {
        return activityRepository.findById(id)
            .filter(activity -> activity.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));
    }

    public Activity updateActivity(Long id, Activity activity) {
        var existing = activityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));
        
        var updated = existing.toBuilder()
            .title(activity.getTitle())
            .description(activity.getDescription())
            .status(activity.getStatus())
            .priority(activity.getPriority())
            .project(activity.getProject())
            .skills(activity.getSkills())
            .estimatedHours(activity.getEstimatedHours())
            .actualHours(activity.getActualHours())
            .build();
        
        if ("DONE".equals(activity.getStatus()) && 
            existing.getCompletedAt() == null) {
            updated = updated.toBuilder()
                .completedAt(LocalDateTime.now())
                .build();
        }
        
        var saved = activityRepository.save(updated);
        log.info("[ACTIVITY] Atualizada: {} (ID: {})", saved.getTitle(), id);
        return saved;
    }

    public void updateActivityStatus(Long id, String status) {
        var activity = activityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));
        
        var updated = activity.toBuilder()
            .status(status)
            .build();
        
        if ("DONE".equals(status) && activity.getCompletedAt() == null) {
            updated = updated.toBuilder()
                .completedAt(LocalDateTime.now())
                .build();
        }
        
        activityRepository.save(updated);
        log.debug("[ACTIVITY] Status atualizado: ID {} -> {}", id, status);
    }
}