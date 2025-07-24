package api.work.profile.controller;

import api.work.profile.entity.DailyReport;
import api.work.profile.service.DailyReportService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/daily-reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;
    private final ProfileService profileService;

    @GetMapping
    public String weeklyReports(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var weeklyReports = dailyReportService.getWeeklyReports(user);
        
        model.addAttribute("weeklyReports", weeklyReports);
        model.addAttribute("user", user);
        return "daily-reports/weekly";
    }
    
    @GetMapping("/new")
    public String newDailyReportForm(@RequestParam(required = false) String date, 
                                   Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        var existingReport = dailyReportService.getReportByDate(user, reportDate);
        
        if (existingReport.isPresent()) {
            model.addAttribute("dailyReport", existingReport.get());
            model.addAttribute("isEdit", true);
        } else {
            var dailyReport = DailyReport.builder()
                    .reportDate(reportDate)
                    .remoteWork(false)
                    .build();
            model.addAttribute("dailyReport", dailyReport);
            model.addAttribute("isEdit", false);
        }
        
        // Adicionar relatórios recentes para contexto
        var recentReports = dailyReportService.getRecentReports(user);
        model.addAttribute("recentReports", recentReports);
        model.addAttribute("user", user);
        return "daily-reports/form";
    }

    @PostMapping
    public String saveDailyReport(@ModelAttribute DailyReport dailyReport, 
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        var user = profileService.getUserFromAuthentication(authentication);
        dailyReport.setUser(user);
        
        // Se é uma edição (tem ID), manter a data original
        if (dailyReport.getId() != null) {
            var existingReport = dailyReportService.getReportById(dailyReport.getId(), user);
            if (existingReport.isPresent()) {
                dailyReport.setReportDate(existingReport.get().getReportDate());
            }
        } else if (dailyReport.getReportDate() == null) {
            dailyReport.setReportDate(LocalDate.now());
        }
        
        dailyReportService.saveDailyReport(dailyReport);
        
        redirectAttributes.addFlashAttribute("successMessage", "Daily Report salvo com sucesso!");
        return "redirect:/daily-reports";
    }
    
    @GetMapping("/{id}")
    public String viewDailyReport(@PathVariable Long id, Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var report = dailyReportService.getReportById(id, user);
        
        if (report.isEmpty()) {
            return "redirect:/daily-reports";
        }
        
        model.addAttribute("dailyReport", report.get());
        model.addAttribute("user", user);
        return "daily-reports/view";
    }
    
    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseBody
    public DailyReport getDailyReportJson(@PathVariable Long id, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var report = dailyReportService.getReportById(id, user);
        return report.orElse(null);
    }
    
    @PostMapping("/{id}/delete")
    public String deleteDailyReport(@PathVariable Long id, Authentication authentication, 
                                   RedirectAttributes redirectAttributes) {
        System.out.println("DELETE request received for report ID: " + id);
        
        var user = profileService.getUserFromAuthentication(authentication);
        System.out.println("User: " + user.getName());
        
        var report = dailyReportService.getReportById(id, user);
        
        if (report.isPresent()) {
            System.out.println("Report found, deleting...");
            dailyReportService.deleteReport(report.get());
            System.out.println("Report deleted successfully");
            redirectAttributes.addFlashAttribute("successMessage", "Daily Report excluído com sucesso!");
        } else {
            System.out.println("Report not found for user");
            redirectAttributes.addFlashAttribute("errorMessage", "Report não encontrado.");
        }
        
        return "redirect:/daily-reports";
    }
}