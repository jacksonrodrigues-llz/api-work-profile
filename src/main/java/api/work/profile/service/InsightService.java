package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.Goal;
import api.work.profile.entity.Achievement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InsightService {
    
    public String generateInsights(List<Activity> activities, List<Goal> goals, List<Achievement> achievements) {
        StringBuilder insights = new StringBuilder();
        
        // Análise de atividades
        long completedActivities = activities.stream().filter(a -> a.getStatus().name().equals("DONE")).count();
        double completionRate = activities.isEmpty() ? 0 : (completedActivities * 100.0) / activities.size();
        
        if (completionRate > 80) {
            insights.append("<li>🎉 Excelente taxa de conclusão de atividades (%.0f%%). Continue mantendo esse ritmo!</li>".formatted(completionRate));
        } else if (completionRate < 50) {
            insights.append("<li>⚠️ Taxa de conclusão baixa (%.0f%%). Considere revisar prioridades e prazos.</li>".formatted(completionRate));
        }
        
        // Análise de metas
        long activeGoals = goals.stream().filter(g -> g.getStatus().name().equals("ACTIVE")).count();
        long completedGoals = goals.stream().filter(g -> g.getStatus().name().equals("COMPLETED")).count();
        
        if (activeGoals > 5) {
            insights.append("<li>📊 Muitas metas ativas (%d). Foque em 3-5 metas principais para melhor resultado.</li>".formatted(activeGoals));
        }
        
        if (completedGoals > 0) {
            insights.append("<li>🎯 %d meta(s) concluída(s). Defina novas metas desafiadoras para continuar crescendo.</li>".formatted(completedGoals));
        }
        
        // Análise de conquistas
        if (achievements.size() < 3) {
            insights.append("<li>🏆 Registre mais conquistas! Documente certificações, projetos e reconhecimentos.</li>");
        }
        
        // Análise de produtividade
        var recentActivities = activities.stream()
            .filter(a -> a.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
            .count();
        
        if (recentActivities == 0) {
            insights.append("<li>📈 Nenhuma atividade registrada nos últimos 30 dias. Mantenha o registro atualizado.</li>");
        }
        
        return insights.toString();
    }
    
    public double[] calculateRadarData(List<Activity> activities, List<Goal> goals, List<Achievement> achievements) {
        double baseLevel = 3.0;
        long completedGoals = goals.stream().filter(g -> g.getStatus().name().equals("COMPLETED")).count();
        long activeGoals = goals.stream().filter(g -> g.getStatus().name().equals("ACTIVE")).count();
        int achievementCount = achievements.size();
        
        return new double[]{
            Math.min(10.0, baseLevel + completedGoals * 1.2 + achievementCount * 0.5), // Técnico
            Math.min(10.0, baseLevel + activeGoals * 1.0 + achievementCount * 0.8),     // Liderança
            Math.min(10.0, baseLevel + achievementCount * 1.0 + completedGoals * 0.3),  // Comunicação
            Math.min(10.0, baseLevel + completedGoals * 0.8 + activeGoals * 0.6),       // Inovação
            Math.min(10.0, baseLevel + achievementCount * 1.2 + activeGoals * 0.4),     // Colaboração
            Math.min(10.0, baseLevel + (completedGoals + activeGoals + achievementCount) * 0.3) // Aprendizado
        };
    }
    
    public double calculateOverallScore(List<Activity> activities, List<Goal> goals, List<Achievement> achievements) {
        double[] radar = calculateRadarData(activities, goals, achievements);
        return java.util.Arrays.stream(radar).average().orElse(0.0);
    }
}