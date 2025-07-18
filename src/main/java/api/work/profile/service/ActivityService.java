package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;

    public Map<String, Object> getKanbanData(User user) {
        var activities = activityRepository.findByUserOrderByCreatedAtDesc(user);
        
        var todoActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.TODO)
            .toList();
        var inProgressActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.IN_PROGRESS)
            .toList();
        var devActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.DEV)
            .toList();
        var uatActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.UAT)
            .toList();
        var hmlActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.HML)
            .toList();
        var prdActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.PRD)
            .toList();
        var deployActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.DEPLOY)
            .toList();
        var doneActivities = activities.stream()
            .filter(a -> a.getStatus() == Activity.ActivityStatus.DONE)
            .toList();
        
        var result = new java.util.HashMap<String, Object>();
        result.put("activities", activities);
        result.put("todoActivities", todoActivities);
        result.put("inProgressActivities", inProgressActivities);
        result.put("devActivities", devActivities);
        result.put("uatActivities", uatActivities);
        result.put("hmlActivities", hmlActivities);
        result.put("prdActivities", prdActivities);
        result.put("deployActivities", deployActivities);
        result.put("doneActivities", doneActivities);
        result.put("todoCount", todoActivities.size());
        result.put("inProgressCount", inProgressActivities.size());
        result.put("devCount", devActivities.size());
        result.put("uatCount", uatActivities.size());
        result.put("hmlCount", hmlActivities.size());
        result.put("prdCount", prdActivities.size());
        result.put("deployCount", deployActivities.size());
        result.put("doneCount", doneActivities.size());
        
        return result;
    }

    public Activity saveActivity(Activity activity, User user) {
        var activityToSave = activity.toBuilder()
            .user(user)
            .build();
        
        if (activityToSave.getStatus() == Activity.ActivityStatus.DONE && 
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
        
        if (activity.getStatus() == Activity.ActivityStatus.DONE && 
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
        
        var newStatus = Activity.ActivityStatus.valueOf(status);
        var updated = activity.toBuilder()
            .status(newStatus)
            .build();
        
        if (newStatus == Activity.ActivityStatus.DONE && activity.getCompletedAt() == null) {
            updated = updated.toBuilder()
                .completedAt(LocalDateTime.now())
                .build();
        }
        
        activityRepository.save(updated);
        log.debug("[ACTIVITY] Status atualizado: ID {} -> {}", id, status);
    }
}