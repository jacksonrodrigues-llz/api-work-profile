package api.work.profile.controller;

import api.work.profile.service.DashboardService;
import api.work.profile.service.DailyReportService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final ProfileService profileService;
    private final DashboardService dashboardService;
    private final DailyReportService dailyReportService;
    
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            if (user == null) {
                log.error("[DASHBOARD] Usuário não encontrado: {}", authentication.getName());
                return "redirect:/login";
            }
            
            model.addAttribute("user", user);
            model.addAttribute("metrics", dashboardService.getDashboardMetrics(user));
            model.addAttribute("dailyReports", dailyReportService.getRecentReports(user));
            model.addAttribute("weeklyReports", dailyReportService.getWeeklyReports(user));
            
            return "dashboard";
        } catch (Exception e) {
            log.error("[DASHBOARD] Erro para {}: {}", authentication.getName(), e.getMessage());
            model.addAttribute("errorMessage", "Erro ao carregar dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }
}