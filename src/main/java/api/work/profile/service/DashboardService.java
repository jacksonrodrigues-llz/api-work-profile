package api.work.profile.service;

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
        Map<String, Object> metrics = new HashMap<>();
        
        // Métricas do mês atual
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Atividades
        Long completedActivities = activityRepository.countCompletedActivitiesSince(user, api.work.profile.entity.Activity.ActivityStatus.DONE, startOfMonth);
        Double avgHours = activityRepository.getAverageHoursPerActivity(user, api.work.profile.entity.Activity.ActivityStatus.DONE);
        
        // Metas
        Long completedGoals = goalRepository.countCompletedGoals(user, api.work.profile.entity.Goal.GoalStatus.COMPLETED);
        Long activeGoals = goalRepository.countActiveGoals(user, api.work.profile.entity.Goal.GoalStatus.ACTIVE);
        
        // GitHub dados
        Integer pullRequests = 0;
        
        if (user.getGithubUsername() != null) {
            pullRequests = gitHubService.getPullRequestCount(user.getGithubUsername());
            log.info("GitHub stats para {}: {} PRs", user.getGithubUsername(), pullRequests);
        }
        
        // Débitos Técnicos
        Long techDebts = techDebtRepository.countByUser(user);
        
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