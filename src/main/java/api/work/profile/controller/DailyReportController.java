package api.work.profile.controller;

import api.work.profile.entity.DailyReport;
import api.work.profile.service.DailyReportService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/daily-reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final ProfileService profileService;

    @GetMapping("/new")
    public String newDailyReportForm(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        
        // Verificar se já existe um relatório para hoje
        var todaysReport = dailyReportService.getTodaysReport(user);
        
        if (todaysReport.isPresent()) {
            model.addAttribute("dailyReport", todaysReport.get());
            model.addAttribute("isEdit", true);
        } else {
            var dailyReport = DailyReport.builder()
                    .reportDate(LocalDate.now())
                    .remoteWork(false)
                    .build();
            model.addAttribute("dailyReport", dailyReport);
            model.addAttribute("isEdit", false);
        }
        
        model.addAttribute("user", user);
        return "daily-reports/form";
    }

    @PostMapping
    public String saveDailyReport(@ModelAttribute DailyReport dailyReport, 
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        var user = profileService.getUserFromAuthentication(authentication);
        dailyReport.setUser(user);
        
        if (dailyReport.getReportDate() == null) {
            dailyReport.setReportDate(LocalDate.now());
        }
        
        dailyReportService.saveDailyReport(dailyReport);
        
        redirectAttributes.addFlashAttribute("successMessage", "Daily Report salvo com sucesso!");
        return "redirect:/dashboard";
    }
}