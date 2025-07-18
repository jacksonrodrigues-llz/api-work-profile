package api.work.profile.service;

import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import api.work.profile.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {

    private final GoalRepository goalRepository;

    public Map<String, Object> getGoalData(User user) {
        var goals = goalRepository.findByUserOrderByCreatedAtDesc(user);
        
        var activeGoals = goals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.ACTIVE)
            .toList();
        var completedGoals = goals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.COMPLETED)
            .toList();
        var pausedGoals = goals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.PAUSED)
            .toList();
        var cancelledGoals = goals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.CANCELLED)
            .toList();
        
        var result = new java.util.HashMap<String, Object>();
        result.put("goals", goals);
        result.put("activeGoals", activeGoals);
        result.put("completedGoals", completedGoals);
        result.put("pausedGoals", pausedGoals);
        result.put("cancelledGoals", cancelledGoals);
        result.put("activeCount", activeGoals.size());
        result.put("completedCount", completedGoals.size());
        result.put("pausedCount", pausedGoals.size());
        result.put("cancelledCount", cancelledGoals.size());
        return result;
    }

    public Goal saveGoal(Goal goal, User user) {
        var goalToSave = goal.toBuilder()
            .user(user)
            .build();
        
        var saved = goalRepository.save(goalToSave);
        log.info("[GOAL] Criada: {} (ID: {})", saved.getTitle(), saved.getId());
        return saved;
    }

    public Goal getGoalForEdit(Long id, User user) {
        return goalRepository.findById(id)
            .filter(goal -> goal.getUser().equals(user))
            .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
    }

    public Goal updateGoal(Long id, Goal goal) {
        var existing = goalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
        
        var updated = existing.toBuilder()
            .title(goal.getTitle())
            .description(goal.getDescription())
            .targetDate(goal.getTargetDate())
            .progressPercentage(goal.getProgressPercentage())
            .status(goal.getStatus())
            .category(goal.getCategory())
            .build();
        
        var saved = goalRepository.save(updated);
        log.info("[GOAL] Atualizada: {} (ID: {})", saved.getTitle(), id);
        return saved;
    }

    public void updateGoalStatus(Long id, String status) {
        var goal = goalRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
        
        var newStatus = Goal.GoalStatus.valueOf(status);
        var updated = goal.toBuilder()
            .status(newStatus)
            .build();
        
        if (newStatus == Goal.GoalStatus.COMPLETED && goal.getCompletedAt() == null) {
            updated = updated.toBuilder()
                .completedAt(LocalDateTime.now())
                .progressPercentage(100)
                .build();
        }
        
        goalRepository.save(updated);
        log.debug("[GOAL] Status atualizado: ID {} -> {}", id, status);
    }
}