package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import api.work.profile.config.GitHubConfig;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.repository.TechDebtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final AchievementRepository achievementRepository;
    private final TechDebtRepository techDebtRepository;
    private final GitHubService gitHubService;
    private final GitHubConfig gitHubConfig;
    
    public Map<String, Object> getDashboardMetrics(User user) {
        log.debug("[DASHBOARD] Calculando métricas para: {}", user.getEmail());
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Métricas do mês atual
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Atividades
        var completedActivities = activityRepository.countCompletedActivitiesSince(user, String.valueOf(Activity.ActivityStatus.DONE), startOfMonth);
        var avgHours = activityRepository.getAverageHoursPerActivity(user, String.valueOf(Activity.ActivityStatus.DONE));
        
        // Metas
        var completedGoals = goalRepository.countCompletedGoals(user, Goal.GoalStatus.COMPLETED);
        var activeGoals = goalRepository.countActiveGoals(user, Goal.GoalStatus.ACTIVE);
        
        // GitHub dados
        var pullRequests = 0;
        if (user.getGithubUsername() != null) {
            pullRequests = gitHubService.getPullRequestCount(user.getGithubUsername());
            log.debug("[DASHBOARD] GitHub: {} PRs para {}", pullRequests, user.getGithubUsername());
        }
        
        // Débitos Técnicos - mostrar total geral
        var techDebts = techDebtRepository.countAll();
        
        metrics.put("completedActivities", completedActivities);
        metrics.put("averageHours", avgHours != null ? avgHours.intValue() : 0);
        metrics.put("completedGoals", completedGoals);
        metrics.put("activeGoals", activeGoals);
        metrics.put("pullRequests", pullRequests);
        metrics.put("commits", 0);
        metrics.put("achievements", achievementRepository.findByUserOrderByAchievedAtDesc(user).size());
        metrics.put("techDebts", techDebts != null ? techDebts : 0);
        

        
        return metrics;
    }
    

}