package api.work.profile.service;

import api.work.profile.entity.Activity;
import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.repository.TechDebtRepository;
import api.work.profile.entity.TechDebt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
    private final TechDebtRepository techDebtRepository;
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
                String statusIcon = getStatusIcon(activity.getStatus());
                String categoryTag = getCategoryTag(activity.getProject());
                activitiesHtml.append(String.format(
                        "<tr><td>%s</td><td>%s</td><td class='status-%s'>%s %s</td><td class='priority-%s'>%s</td><td>%s</td><td>%s</td></tr>",
                        sanitizeHtml(activity.getTitle()),
                        sanitizeHtml(activity.getProject() != null ? activity.getProject() : "-"),
                        activity.getStatus().toLowerCase(),
                        statusIcon,
                        translateStatus(activity.getStatus()),
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
        
        var completedActivities = activityRepository.countCompletedActivitiesSince(user, String.valueOf(Activity.ActivityStatus.DONE), startOfMonth);
        var avgHours = activityRepository.getAverageHoursPerActivity(user, String.valueOf(Activity.ActivityStatus.DONE));
        var completedGoals = goalRepository.countCompletedGoals(user, Goal.GoalStatus.COMPLETED);
        var activeGoals = goalRepository.countActiveGoals(user, Goal.GoalStatus.ACTIVE);
        var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
        
        report.put("completedActivities", completedActivities != null ? completedActivities : 0);
        report.put("averageHours", avgHours != null ? avgHours.intValue() : 0);
        report.put("completedGoals", completedGoals != null ? completedGoals : 0);
        report.put("activeGoals", activeGoals != null ? activeGoals : 0);
        report.put("achievements", achievements != null ? achievements.size() : 0);
        
        return report;
    }
    
    public Map<String, Object> getReportData(User user) {
        return getReportData(user, null);
    }
    
    public Map<String, Object> getReportData(User user, String periodo) {
        var data = new HashMap<String, Object>();
        
        LocalDateTime periodoDate = calculatePeriodDate(periodo);
        
        data.put("monthlyReport", getMonthlyReport(user, periodoDate));
        data.put("activities", getFilteredActivities(user, periodoDate));
        data.put("goals", getFilteredGoals(user, periodoDate));
        data.put("achievements", getFilteredAchievements(user, periodoDate));
        data.put("chartData", getChartData(user, periodoDate));
        
        return data;
    }
    
    public Map<String, Object> getDtReportData(User user, String periodo) {
        var data = new HashMap<String, Object>();
        
        try {
            // Buscar dados reais de débitos técnicos
            var allDebts = techDebtRepository.findByUserOrderByDataCriacaoDesc(user, org.springframework.data.domain.Pageable.unpaged()).getContent();
            log.info("[DT_REPORT] Encontrados {} débitos técnicos para usuário {}", allDebts.size(), user.getEmail());
            
            // Filtrar por período se especificado
            LocalDateTime periodoDate = calculatePeriodDate(periodo);
            if (periodoDate != null) {
                allDebts = allDebts.stream()
                    .filter(debt -> debt.getDataCriacao() != null && debt.getDataCriacao().isAfter(periodoDate))
                    .toList();
                log.info("[DT_REPORT] Após filtro de período: {} débitos", allDebts.size());
            }
            
            // Métricas
            long criticalOpen = allDebts.stream().filter(d -> d.getPrioridade() == 1 && d.getStatus() != TechDebt.StatusDebito.DONE).count();
            double averageTime = allDebts.stream().mapToLong(TechDebt::getDiasEmAberto).average().orElse(0);
            long resolved = allDebts.stream().filter(d -> d.getStatus() == TechDebt.StatusDebito.DONE).count();
            int resolutionRate = allDebts.isEmpty() ? 0 : (int) ((resolved * 100) / allDebts.size());
            
            data.put("criticalOpen", criticalOpen);
            data.put("averageTime", (int) averageTime);
            data.put("resolved", resolved);
            data.put("resolutionRate", resolutionRate);
            
            // Dados por criador
            var creatorMap = allDebts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    debt -> debt.getCriadoPor() != null && !debt.getCriadoPor().trim().isEmpty() ? debt.getCriadoPor() : "Desconhecido",
                    java.util.stream.Collectors.counting()
                ));
            
            if (creatorMap.isEmpty() || allDebts.isEmpty()) {
                data.put("creatorData", Map.of(
                    "labels", List.of("Sem dados"),
                    "data", List.of(1),
                    "backgroundColor", List.of("#6c757d")
                ));
            } else {
                var colors = List.of("#ff6b35", "#ff8c42", "#ffa726", "#ffb74d", "#ffcc80", "#ffe0b2");
                var labels = creatorMap.keySet().stream().sorted().toList();
                var dataValues = labels.stream().map(creatorMap::get).toList();
                var backgroundColors = new java.util.ArrayList<String>();
                for (int i = 0; i < labels.size(); i++) {
                    backgroundColors.add(colors.get(i % colors.size()));
                }
                
                data.put("creatorData", Map.of(
                    "labels", labels,
                    "data", dataValues,
                    "backgroundColor", backgroundColors
                ));
            }
            
            // Dados por status
            var statusCounts = new long[7]; // Incluindo PAUSE e CANCELLED
            for (var debt : allDebts) {
                if (debt.getStatus() != null) {
                    switch (debt.getStatus()) {
                        case TODO -> statusCounts[0]++;
                        case IN_PROGRESS -> statusCounts[1]++;
                        case PAUSE -> statusCounts[2]++;
                        case TEST -> statusCounts[3]++;
                        case DEPLOY -> statusCounts[4]++;
                        case DONE -> statusCounts[5]++;
                        case CANCELLED -> statusCounts[6]++;
                    }
                }
            }
            
            data.put("statusData", Map.of(
                "labels", List.of("TODO", "EM PROGRESSO", "PAUSADO", "TESTE", "DEPLOY", "CONCLUÍDO", "CANCELADO"),
                "data", java.util.Arrays.stream(statusCounts).boxed().toList(),
                "backgroundColor", List.of("#6c757d", "#ffc107", "#fd7e14", "#17a2b8", "#20c997", "#28a745", "#dc3545")
            ));
            
            // Tendência mensal
            var now = LocalDateTime.now();
            var monthNames = new java.util.ArrayList<String>();
            var monthlyCreated = new java.util.ArrayList<Long>();
            var monthlyResolved = new java.util.ArrayList<Long>();
            
            for (int i = 5; i >= 0; i--) {
                var monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                var monthEnd = monthStart.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                
                monthNames.add(monthStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.forLanguageTag("pt-BR"))));
                
                long created = allDebts.stream()
                    .filter(d -> d.getDataCriacao() != null)
                    .filter(d -> !d.getDataCriacao().isBefore(monthStart) && !d.getDataCriacao().isAfter(monthEnd))
                    .count();
                
                long monthlyResolvedCount = allDebts.stream()
                    .filter(d -> d.getDataResolucao() != null)
                    .filter(d -> !d.getDataResolucao().isBefore(monthStart) && !d.getDataResolucao().isAfter(monthEnd))
                    .count();
                
                monthlyCreated.add(created);
                monthlyResolved.add(monthlyResolvedCount);
            }
            
            data.put("trendData", Map.of(
                "labels", monthNames,
                "created", monthlyCreated,
                "resolved", monthlyResolved
            ));
            
            // Dados por tipo
            var typeCounts = new java.util.HashMap<String, Long>();
            typeCounts.put("Performance", 0L);
            typeCounts.put("Segurança", 0L);
            typeCounts.put("Código", 0L);
            typeCounts.put("Arquitetura", 0L);
            typeCounts.put("Documentação", 0L);
            
            for (var debt : allDebts) {
                if (debt.getTipos() != null && !debt.getTipos().isEmpty()) {
                    for (var tipo : debt.getTipos()) {
                        switch (tipo) {
                            case BACKEND, FRONTEND -> typeCounts.merge("Código", 1L, Long::sum);
                            case SECURITY -> typeCounts.merge("Segurança", 1L, Long::sum);
                            case INFRA -> typeCounts.merge("Arquitetura", 1L, Long::sum);
                            case DATABASE -> typeCounts.merge("Performance", 1L, Long::sum);
                            default -> typeCounts.merge("Documentação", 1L, Long::sum);
                        }
                    }
                } else {
                    typeCounts.merge("Documentação", 1L, Long::sum);
                }
            }
            
            data.put("typeData", Map.of(
                "labels", List.of("Performance", "Segurança", "Código", "Arquitetura", "Documentação"),
                "data", typeCounts.values().stream().toList(),
                "backgroundColor", List.of("#ff6b35", "#dc3545", "#007bff", "#6f42c1", "#20c997")
            ));
            
            // Dados por prioridade
            var priorityCounts = new long[4];
            for (var debt : allDebts) {
                if (debt.getPrioridade() != null) {
                    int priority = debt.getPrioridade();
                    if (priority >= 1 && priority <= 4) {
                        priorityCounts[priority - 1]++;
                    }
                }
            }
            
            data.put("priorityData", Map.of(
                "labels", List.of("Crítico (1)", "Alto (2)", "Médio (3)", "Baixo (4)"),
                "data", java.util.Arrays.stream(priorityCounts).boxed().toList(),
                "backgroundColor", List.of("#dc3545", "#fd7e14", "#ffc107", "#28a745")
            ));
            
            log.info("[DT_REPORT] Relatório gerado com sucesso: {} débitos processados, críticos: {}, resolvidos: {}, prioridades: {}", 
                allDebts.size(), criticalOpen, resolved, java.util.Arrays.toString(priorityCounts));
            
        } catch (Exception e) {
            log.error("[DT_REPORT] Erro ao gerar relatório DT: {}", e.getMessage(), e);
            // Retornar dados vazios em caso de erro
            data.put("criticalOpen", 0);
            data.put("averageTime", 0);
            data.put("resolved", 0);
            data.put("resolutionRate", 0);
            data.put("creatorData", Map.of("labels", List.of("Erro"), "data", List.of(0)));
            data.put("statusData", Map.of("labels", List.of("Erro"), "data", List.of(0)));
            data.put("trendData", Map.of("labels", List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"), "created", List.of(0,0,0,0,0,0), "resolved", List.of(0,0,0,0,0,0)));
            data.put("typeData", Map.of("labels", List.of("Erro"), "data", List.of(0)));
        }
        
        return data;
    }
    
    private LocalDateTime calculatePeriodDate(String periodo) {
        if (periodo == null || periodo.isEmpty()) return null;
        
        LocalDateTime now = LocalDateTime.now();
        return switch (periodo) {
            case "semana" -> now.minusWeeks(1);
            case "15dias" -> now.minusDays(15);
            case "30dias" -> now.minusDays(30);
            case "3meses" -> now.minusMonths(3);
            case "6meses" -> now.minusMonths(6);
            case "1ano" -> now.minusYears(1);
            default -> null;
        };
    }
    
    private int calculatePeriodMultiplier(String periodo) {
        if (periodo == null || periodo.isEmpty()) return 1;
        
        return switch (periodo) {
            case "semana" -> 1;
            case "15dias" -> 1;
            case "30dias" -> 2;
            case "3meses" -> 3;
            case "6meses" -> 4;
            case "1ano" -> 5;
            default -> 1;
        };
    }
    
    private Map<String, Object> getMonthlyReport(User user, LocalDateTime since) {
        var report = new HashMap<String, Object>();
        var startDate = since != null ? since : LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Buscar dados reais do banco
        var allActivities = activityRepository.findByUserOrderByCreatedAtDesc(user);
        var filteredActivities = since != null ? 
            allActivities.stream().filter(a -> a.getCreatedAt().isAfter(since)).toList() : allActivities;
        
        var completedActivities = filteredActivities.stream()
            .filter(a -> "DONE".equals(a.getStatus()))
            .count();
        
        var totalHours = filteredActivities.stream()
            .filter(a -> a.getActualHours() != null)
            .mapToInt(Activity::getActualHours)
            .sum();
        
        var allGoals = goalRepository.findByUserOrderByCreatedAtDesc(user);
        var filteredGoals = since != null ? 
            allGoals.stream().filter(g -> g.getCreatedAt().isAfter(since)).toList() : allGoals;
        
        var completedGoals = filteredGoals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.COMPLETED)
            .count();
        
        var activeGoals = filteredGoals.stream()
            .filter(g -> g.getStatus() == Goal.GoalStatus.ACTIVE)
            .count();
        
        var allAchievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
        var filteredAchievements = since != null ? 
            allAchievements.stream().filter(a -> a.getAchievedAt() != null && a.getAchievedAt().isAfter(since)).toList() : allAchievements;
        
        report.put("completedActivities", completedActivities);
        report.put("totalHours", totalHours);
        report.put("averageHours", completedActivities > 0 ? totalHours / completedActivities : 0);
        report.put("completedGoals", completedGoals);
        report.put("activeGoals", activeGoals);
        report.put("achievements", filteredAchievements.size());
        
        return report;
    }
    
    private List<Map<String, Object>> getFilteredActivities(User user, LocalDateTime since) {
        var activities = activityRepository.findByUserOrderByCreatedAtDesc(user);
        if (since != null) {
            activities = activities.stream()
                .filter(a -> a.getCreatedAt().isAfter(since))
                .toList();
        }
        return activities.stream().limit(5)
            .map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("title", a.getTitle());
                map.put("project", a.getProject() != null ? a.getProject() : "");
                map.put("status", a.getStatus());
                map.put("priority", a.getPriority().name());
                map.put("createdAt", a.getCreatedAt());
                return map;
            })
            .toList();
    }
    
    private List<Map<String, Object>> getFilteredGoals(User user, LocalDateTime since) {
        var goals = goalRepository.findByUserOrderByCreatedAtDesc(user);
        if (since != null) {
            goals = goals.stream()
                .filter(g -> g.getCreatedAt().isAfter(since))
                .toList();
        }
        return goals.stream().limit(5)
            .map(g -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", g.getId());
                map.put("title", g.getTitle());
                map.put("category", g.getCategory().toString());
                map.put("status", g.getStatus().name());
                map.put("progressPercentage", g.getProgressPercentage() != null ? g.getProgressPercentage() : 0);
                map.put("createdAt", g.getCreatedAt());
                return map;
            })
            .toList();
    }
    
    private List<Map<String, Object>> getFilteredAchievements(User user, LocalDateTime since) {
        var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
        if (since != null) {
            achievements = achievements.stream()
                .filter(a -> a.getAchievedAt() != null && a.getAchievedAt().isAfter(since))
                .toList();
        }
        return achievements.stream().limit(5)
            .map(a -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", a.getId());
                map.put("title", a.getTitle());
                map.put("type", a.getType().name());
                map.put("description", a.getDescription() != null ? a.getDescription() : "");
                map.put("achievedAt", a.getAchievedAt());
                return map;
            })
            .toList();
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
    
    private Map<String, Object> getChartData(User user, LocalDateTime since) {
        var chartData = new HashMap<String, Object>();
        
        try {
            var allActivities = activityRepository.findByUserOrderByCreatedAtDesc(user);
            var allGoals = goalRepository.findByUserOrderByCreatedAtDesc(user);
            var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user);
            
            log.info("[CHART_DATA] Usuário {}: {} atividades, {} metas, {} conquistas", 
                user.getEmail(), allActivities.size(), allGoals.size(), achievements.size());
            
            // Dados para gráfico de progresso mensal
            var now = LocalDateTime.now();
            var monthNames = new java.util.ArrayList<String>();
            var monthlyActivities = new java.util.ArrayList<Long>();
            var monthlyGoals = new java.util.ArrayList<Long>();
            
            for (int i = 5; i >= 0; i--) {
                var monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                var monthEnd = monthStart.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);
                
                monthNames.add(monthStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM", java.util.Locale.forLanguageTag("pt-BR"))));
                
                long activitiesCount = allActivities.stream()
                    .filter(a -> "DONE".equals(a.getStatus()))
                    .filter(a -> a.getCompletedAt() != null)
                    .filter(a -> !a.getCompletedAt().isBefore(monthStart) && !a.getCompletedAt().isAfter(monthEnd))
                    .count();
                
                long goalsCount = allGoals.stream()
                    .filter(g -> g.getStatus() == Goal.GoalStatus.COMPLETED)
                    .filter(g -> g.getCompletedAt() != null)
                    .filter(g -> !g.getCompletedAt().isBefore(monthStart) && !g.getCompletedAt().isAfter(monthEnd))
                    .count();
                
                monthlyActivities.add(activitiesCount);
                monthlyGoals.add(goalsCount);
            }
            
            chartData.put("progressData", Map.of(
                "labels", monthNames,
                "activities", monthlyActivities,
                "goals", monthlyGoals
            ));
            
            // Dados para radar de habilidades (baseado em atividades e metas)
            var techActivities = allActivities.stream()
                .filter(a -> a.getProject() != null && 
                    (a.getProject().toLowerCase().contains("api") || 
                     a.getProject().toLowerCase().contains("backend") || 
                     a.getProject().toLowerCase().contains("frontend") ||
                     a.getProject().toLowerCase().contains("tech")))
                .count();
            
            var leadershipGoals = allGoals.stream()
                .filter(g -> g.getCategory() == Goal.GoalCategory.LEADERSHIP)
                .count();
            
            var technicalGoals = allGoals.stream()
                .filter(g -> g.getCategory() == Goal.GoalCategory.TECHNICAL)
                .count();
            
            var communicationAchievements = achievements.stream()
                .filter(a -> a.getType().name().contains("RECOGNITION") || a.getType().name().contains("LEADERSHIP"))
                .count();
            
            // Calcular valores do radar (0-10)
            var radarValues = List.of(
                Math.min(10L, Math.max(1L, techActivities + technicalGoals)), // Técnico
                Math.min(10L, Math.max(1L, leadershipGoals * 2)), // Liderança
                Math.min(10L, Math.max(1L, communicationAchievements * 2)), // Comunicação
                Math.min(10L, Math.max(1L, achievements.size())), // Inovação
                Math.min(10L, Math.max(1L, allActivities.size() / 5)), // Colaboração
                Math.min(10L, Math.max(1L, (allGoals.size() + achievements.size()) / 2)) // Aprendizado
            );
            
            chartData.put("radarData", Map.of(
                "labels", List.of("Técnico", "Liderança", "Comunicação", "Inovação", "Colaboração", "Aprendizado"),
                "data", radarValues
            ));
            
            // Dados para radar de performance
            var avgHoursPerActivity = allActivities.isEmpty() ? 0 : 
                allActivities.stream().filter(a -> a.getActualHours() != null)
                    .mapToInt(Activity::getActualHours).average().orElse(0);
            
            var completionRate = allActivities.isEmpty() ? 0 : 
                (double) allActivities.stream().filter(a -> "DONE".equals(a.getStatus())).count() / allActivities.size() * 10;
            
            var goalCompletionRate = allGoals.isEmpty() ? 0 : 
                (double) allGoals.stream().filter(g -> g.getStatus() == Goal.GoalStatus.COMPLETED).count() / allGoals.size() * 10;
            
            var performanceValues = List.of(
                Math.min(10L, Math.max(1L, Math.round(completionRate))), // Produtividade
                Math.min(10L, Math.max(1L, Math.round(goalCompletionRate))), // Qualidade
                Math.min(10L, Math.max(1L, Math.round(avgHoursPerActivity / 2))), // Velocidade
                Math.min(10L, Math.max(1L, achievements.size() > 0 ? 8L : 5L)), // Consistência
                Math.min(10L, Math.max(1L, (techActivities + achievements.size()) / 2)) // Eficiência
            );
            
            chartData.put("performanceData", Map.of(
                "labels", List.of("Produtividade", "Qualidade", "Velocidade", "Consistência", "Eficiência"),
                "data", performanceValues
            ));
            
            log.info("[CHART_DATA] Radar values: {}", radarValues);
            log.info("[CHART_DATA] Performance values: {}", performanceValues);
            log.info("[CHART_DATA] Progress data: activities={}, goals={}", monthlyActivities, monthlyGoals);
            
        } catch (Exception e) {
            log.error("[CHART_DATA] Erro ao gerar dados de gráficos: {}", e.getMessage(), e);
            chartData.put("progressData", Map.of(
                "labels", List.of("Jan", "Fev", "Mar", "Abr", "Mai", "Jun"),
                "activities", List.of(0L, 0L, 0L, 0L, 0L, 0L),
                "goals", List.of(0L, 0L, 0L, 0L, 0L, 0L)
            ));
            chartData.put("radarData", Map.of(
                "labels", List.of("Técnico", "Liderança", "Comunicação", "Inovação", "Colaboração", "Aprendizado"),
                "data", List.of(1L, 1L, 1L, 1L, 1L, 1L)
            ));
        }
        
        return chartData;
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