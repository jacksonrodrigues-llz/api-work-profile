package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    private static final int MAX_ACTIVITIES_TO_SHOW = 10;
    private static final int MAX_GOALS_TO_SHOW = 8;
    private static final int MAX_ACHIEVEMENTS_TO_SHOW = 8;

    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final AchievementRepository achievementRepository;
    private final InsightService insightService;

    public byte[] generatePdfReport(User user) {
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("Usuário inválido para geração do relatório");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            log.info("[REPORT] Gerando PDF para: {}", user.getEmail());

            String html = generateHtmlReport(user);
            if (html == null || html.trim().isEmpty()) {
                throw new IllegalStateException("HTML gerado está vazio");
            }


            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new IllegalStateException("PDF gerado está vazio");
            }

            log.info("[REPORT] PDF gerado: {} bytes", pdfBytes.length);
            return pdfBytes;
        } catch (Exception e) {
            log.error("[REPORT] Erro ao gerar PDF para {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private String generateHtmlReport(User user) {
        try {
            var activities = activityRepository.findByUserOrderByCreatedAtDesc(user);
            var goals = goalRepository.findByUserOrderByCreatedAtDesc(user);
            var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
            
            log.debug("[REPORT] Dados: {} atividades, {} metas, {} conquistas", 
                     activities.size(), goals.size(), achievements.size());

            var insights = insightService.generateInsights(activities, goals, achievements);
            var radarData = insightService.calculateRadarData(activities, goals, achievements);
            StringBuilder activitiesHtml = new StringBuilder();
            activities.stream().limit(MAX_ACTIVITIES_TO_SHOW).forEach(activity -> {
                String statusIcon = getStatusIcon(activity.getStatus().name());
                String categoryTag = getCategoryTag(activity.getProject());
                activitiesHtml.append(String.format(
                        "<tr><td>%s</td><td>%s</td><td class='status-%s'>%s %s</td><td class='priority-%s'>%s</td><td>%s</td><td>%s</td></tr>",
                        sanitizeHtml(activity.getTitle()),
                        sanitizeHtml(activity.getProject() != null ? activity.getProject() : "-"),
                        activity.getStatus().name().toLowerCase(),
                        statusIcon,
                        translateStatus(activity.getStatus().name()),
                        activity.getPriority().name().toLowerCase(),
                        translatePriority(activity.getPriority().name()),
                        activity.getActualHours() != null ? activity.getActualHours() + "h" : "-",
                        categoryTag
                ));
            });


            StringBuilder goalsHtml = new StringBuilder();
            goals.stream().limit(MAX_GOALS_TO_SHOW).forEach(goal -> {
                int progress = goal.getProgressPercentage() != null ? goal.getProgressPercentage() : 0;
                String goalType = determineGoalType(goal.getCategory().toString());
                goalsHtml.append(String.format(
                        "<tr><td>%s</td><td>%s</td><td>%s</td><td><div class='progress-bar'><div class='progress-fill' style='width: %d%%'></div></div> %d%%</td><td>%s</td></tr>",
                        sanitizeHtml(goal.getTitle()),
                        sanitizeHtml(goal.getCategory() != null ? goal.getCategory().toString() : "-"),
                        translateGoalStatus(goal.getStatus().name()),
                        progress,
                        progress,
                        goalType
                ));
            });


            StringBuilder achievementsHtml = new StringBuilder();
            achievements.stream().limit(MAX_ACHIEVEMENTS_TO_SHOW).forEach(achievement -> {
                String description = achievement.getDescription() != null ?
                        (achievement.getDescription().length() > 50 ?
                                achievement.getDescription().substring(0, 50) + "..." :
                                achievement.getDescription()) : "-";
                achievementsHtml.append(String.format(
                        "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                        sanitizeHtml(achievement.getTitle()),
                        translateAchievementType(achievement.getType().name()),
                        achievement.getAchievedAt() != null ? achievement.getAchievedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                        sanitizeHtml(description)
                ));
            });


            String htmlResult = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>Relatório Profissional - %s</title>
                    <style>
                        @page { margin: 10mm; size: A4; }
                        body { font-family: Arial, sans-serif; margin: 0; padding: 0; color: #1a202c; line-height: 1.4; font-size: 12px; }
                        
                        .header { background: #005cb9; color: white; padding: 15px; margin-bottom: 15px; }
                        .header-table { width: 100%%; border-collapse: collapse; }
                        .header-left { width: 70%%; vertical-align: top; }
                        .header-right { width: 30%%; vertical-align: top; text-align: right; }
                        .logo { font-size: 18px; font-weight: bold; margin-bottom: 5px; }
                        .user-name { font-size: 14px; font-weight: bold; margin-bottom: 3px; }
                        .user-details { font-size: 11px; }
                        
                        .kpi-section { margin: 15px 0; }
                        .kpi-table { width: 100%%; border-collapse: collapse; }
                        .kpi-card { text-align: center; padding: 10px; background: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #005cb9; width: 20%%; }
                        .kpi-value { font-size: 16px; font-weight: bold; color: #005cb9; margin: 0; }
                        .kpi-label { font-size: 9px; color: #4a5568; margin-top: 4px; }
                        
                        .section { margin: 15px 0; background: white; padding: 15px; border: 1px solid #e2e8f0; }
                        .section-title { color: #005cb9; font-size: 13px; font-weight: bold; margin-bottom: 10px; border-bottom: 2px solid #005cb9; padding-bottom: 6px; }
                        
                        table { width: 100%%; border-collapse: collapse; font-size: 10px; }
                        th, td { padding: 6px 4px; text-align: left; border-bottom: 1px solid #e2e8f0; }
                        th { background: #005cb9; color: white; font-weight: bold; font-size: 9px; }
                        tbody tr:nth-child(even) { background: #f8fafc; }
                        
                        .status-done { color: #38a169; font-weight: bold; }
                        .status-in_progress { color: #d69e2e; font-weight: bold; }
                        .status-test { color: #3182ce; font-weight: bold; }
                        .priority-high { color: #e53e3e; font-weight: bold; }
                        .priority-medium { color: #dd6b20; font-weight: bold; }
                        .priority-low { color: #38a169; font-weight: bold; }
                        
                        .progress-bar { height: 8px; background: #e2e8f0; border: 1px solid #ccc; }
                        .progress-fill { height: 100%%; background: #005cb9; }
                        
                        .tag { background: #005cb9; color: white; padding: 2px 6px; font-size: 8px; font-weight: bold; }
                        
                        .footer { text-align: center; margin-top: 15px; padding: 10px; background: #f8fafc; font-size: 10px; color: #4a5568; border-top: 2px solid #005cb9; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <table class="header-table">
                            <tr>
                                <td class="header-left">
                                    <div class="logo">&lt;/&gt; DevPortal</div>
                                    <div class="user-name">%s</div>
                                    <div class="user-details">%s | %s</div>
                                </td>
                                <td class="header-right">
                                    <strong>Relatório Profissional</strong><br/>
                                    Gerado em %s
                                </td>
                            </tr>
                        </table>
                    </div>
                    
                    <div class="kpi-section">
                        <table class="kpi-table">
                            <tr>
                                <td class="kpi-card">
                                    <div class="kpi-value">%d</div>
                                    <div class="kpi-label">Atividades Concluídas</div>
                                </td>
                                <td class="kpi-card">
                                    <div class="kpi-value">%d</div>
                                    <div class="kpi-label">Horas Investidas</div>
                                </td>
                                <td class="kpi-card">
                                    <div class="kpi-value">%d</div>
                                    <div class="kpi-label">Metas Atingidas</div>
                                </td>
                                <td class="kpi-card">
                                    <div class="kpi-value">%d</div>
                                    <div class="kpi-label">Conquistas</div>
                                </td>
                                <td class="kpi-card">
                                    <div class="kpi-value">%d</div>
                                    <div class="kpi-label">Prioridade Média</div>
                                </td>
                            </tr>
                        </table>
                    </div>

                    
                    <div class="section">
                        <h2 class="section-title">Atividades Realizadas</h2>
                        <table>
                            <thead>
                                <tr><th>Título</th><th>Projeto</th><th>Status</th><th>Prioridade</th><th>Horas</th><th>Categoria</th></tr>
                            </thead>
                            <tbody>%s</tbody>
                        </table>
                    </div>
                    
                    <div class="section">
                        <h2 class="section-title">Metas e Objetivos</h2>
                        <table>
                            <thead>
                                <tr><th>Título</th><th>Categoria</th><th>Status</th><th>Progresso</th><th>Tipo de Meta</th></tr>
                            </thead>
                            <tbody>%s</tbody>
                        </table>
                    </div>
                    
                    <div class="section">
                        <h2 class="section-title">Conquistas e Marcos</h2>
                        <table>
                            <thead>
                                <tr><th>Título</th><th>Tipo</th><th>Data</th><th>Descrição Breve</th></tr>
                            </thead>
                            <tbody>%s</tbody>
                        </table>
                    </div>
                    
                    <div class="footer">
                        <strong>DevPortal</strong> - Portal de Progresso Profissional<br/>
                        Relatório gerado automaticamente - %s
                    </div>
                </body>
                </html>
                """,
                    sanitizeHtml(user.getName()),
                    sanitizeHtml(user.getName()),
                    sanitizeHtml(user.getCompany() != null ? user.getCompany() : "Profissional"),
                    sanitizeHtml(user.getEmail()),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
                    activities.size(),
                    Math.round(calculateTotalHours(activities)),
                    goals.size(),
                    achievements.size(),
                    Math.round(calculateAveragePriority(activities)),
                    activitiesHtml.toString(),
                    goalsHtml.toString(),
                    achievementsHtml.toString(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT))
            );


            return htmlResult;

        } catch (Exception e) {
            log.error("[REPORT] Erro ao gerar HTML: {}", e.getMessage());
            throw new RuntimeException("Erro ao gerar HTML: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getMonthlyReport(User user) {
        var report = new HashMap<String, Object>();
        var startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        var completedActivities = activityRepository.countCompletedActivitiesSince(user, api.work.profile.entity.Activity.ActivityStatus.DONE, startOfMonth);
        var avgHours = activityRepository.getAverageHoursPerActivity(user, api.work.profile.entity.Activity.ActivityStatus.DONE);
        var completedGoals = goalRepository.countCompletedGoals(user, api.work.profile.entity.Goal.GoalStatus.COMPLETED);
        var activeGoals = goalRepository.countActiveGoals(user, api.work.profile.entity.Goal.GoalStatus.ACTIVE);
        var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
        
        report.put("completedActivities", completedActivities != null ? completedActivities : 0);
        report.put("averageHours", avgHours != null ? avgHours.intValue() : 0);
        report.put("completedGoals", completedGoals != null ? completedGoals : 0);
        report.put("activeGoals", activeGoals != null ? activeGoals : 0);
        report.put("achievements", achievements != null ? achievements.size() : 0);
        
        return report;
    }
    
    public Map<String, Object> getReportData(User user) {
        var data = new HashMap<String, Object>();
        
        data.put("monthlyReport", getMonthlyReport(user));
        data.put("activities", activityRepository.findByUserOrderByCreatedAtDesc(user).stream().limit(5).toList());
        data.put("goals", goalRepository.findByUserOrderByCreatedAtDesc(user).stream().limit(5).toList());
        data.put("achievements", achievementRepository.findByUserOrderByAchievedAtDesc(user).stream().limit(5).toList());
        
        return data;
    }
    
    private int calculateTotalHours(java.util.List<api.work.profile.entity.Activity> activities) {
        try {
            return activities.stream()
                .filter(a -> a.getActualHours() != null)
                .mapToInt(api.work.profile.entity.Activity::getActualHours)
                .sum();
        } catch (Exception e) {
            log.warn("[REPORT] Erro ao calcular horas: {}", e.getMessage());
            return 0;
        }
    }
    
    private double calculateAveragePriority(java.util.List<api.work.profile.entity.Activity> activities) {
        try {
            if (activities.isEmpty()) return 0.0;
            double sum = activities.stream()
                .mapToDouble(a -> {
                    switch (a.getPriority().name()) {
                        case "LOW": return 1.0;
                        case "MEDIUM": return 2.0;
                        case "HIGH": return 3.0;
                        case "URGENT": return 4.0;
                        default: return 2.0;
                    }
                })
                .sum();
            return sum / activities.size();
        } catch (Exception e) {
            log.warn("[REPORT] Erro ao calcular prioridade: {}", e.getMessage());
            return 0.0;
        }
    }
    
    private String getStatusIcon(String status) {
        switch (status) {
            case "DONE": return "✅";
            case "IN_PROGRESS": return "🔄";
            case "TEST": return "🧪";
            case "DEPLOY": return "🚀";
            default: return "📋";
        }
    }
    
    private String getCategoryTag(String project) {
        if (project == null) return "<span class='tag'>GERAL</span>";
        if (project.toLowerCase().contains("api")) return "<span class='tag'>BACKEND</span>";
        if (project.toLowerCase().contains("test")) return "<span class='tag'>TEST</span>";
        return "<span class='tag'>FULL-STACK</span>";
    }
    
    private String translateStatus(String status) {
        switch (status) {
            case "DONE": return "CONCLUÍDO";
            case "IN_PROGRESS": return "EM PROGRESSO";
            case "TEST": return "TESTE";
            case "DEPLOY": return "DEPLOY";
            default: return "A FAZER";
        }
    }
    
    private String translatePriority(String priority) {
        switch (priority) {
            case "LOW": return "Baixa";
            case "MEDIUM": return "Média";
            case "HIGH": return "Alta";
            case "URGENT": return "Urgente";
            default: return "Média";
        }
    }
    
    private String translateGoalStatus(String status) {
        switch (status) {
            case "ACTIVE": return "Em Andamento";
            case "COMPLETED": return "✅ Concluído";
            case "PAUSED": return "Pausado";
            case "CANCELLED": return "Cancelado";
            default: return "Em Andamento";
        }
    }
    
    private String determineGoalType(String category) {
        if (category == null) return "Crescimento Pessoal";
        switch (category.toUpperCase()) {
            case "TECHNICAL": return "Skill Técnica";
            case "LEADERSHIP": return "Crescimento Pessoal";
            case "PROCESS": return "Melhoria Processual";
            default: return "Qualidade de Código";
        }
    }
    
    private String translateAchievementType(String type) {
        switch (type) {
            case "MILESTONE": return "Marco";
            case "CERTIFICATION": return "Certificação";
            case "RECOGNITION": return "Reconhecimento";
            case "INNOVATION": return "Inovação";
            case "LEADERSHIP": return "Liderança";
            case "TECHNICAL": return "Técnica";
            default: return "Geral";
        }
    }
    
    private String sanitizeHtml(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim()
                  .replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;")
                  .replace("\n", "<br/>");
    }
    
    private double calculateGoalProgress(java.util.List<api.work.profile.entity.Goal> goals) {
        try {
            if (goals.isEmpty()) return 0.0;
            double totalProgress = goals.stream()
                .mapToDouble(g -> g.getProgressPercentage() != null ? g.getProgressPercentage() : 0.0)
                .sum();
            return totalProgress / goals.size();
        } catch (Exception e) {
            log.warn("[REPORT] Erro ao calcular progresso: {}", e.getMessage());
            return 0.0;
        }
    }
}