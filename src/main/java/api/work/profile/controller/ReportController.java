package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.service.ReportService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    private final UserService userService;
    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final AchievementRepository achievementRepository;
    
    @GetMapping
    public String reports(Authentication authentication, Model model) {
        try {
            User user = getUserFromAuthentication(authentication);
            log.debug("[REPORT] Carregando relatórios para: {}", user.getEmail());
            
            model.addAttribute("user", user);
            model.addAttribute("currentUser", user);
            model.addAttribute("monthlyReport", reportService.getMonthlyReport(user));
            
            // Listas detalhadas para exibição
            var activities = activityRepository.findByUserOrderByCreatedAtDesc(user).stream().limit(5).toList();
            var goals = goalRepository.findByUserOrderByCreatedAtDesc(user).stream().limit(5).toList();
            var achievements = achievementRepository.findByUserOrderByAchievedAtDesc(user).stream().limit(5).toList();
            
            model.addAttribute("activities", activities);
            model.addAttribute("goals", goals);
            model.addAttribute("achievements", achievements);
            
            return "reports/index";
        } catch (Exception e) {
            log.error("[REPORT] Erro ao carregar relatórios: {}", e.getMessage());
            model.addAttribute("errorMessage", "Erro ao carregar relatórios: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        byte[] pdfBytes = reportService.generatePdfReport(user);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-profissional.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            return userService.findOrCreateUser((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal());
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return userService.findByEmail(userDetails.getUsername());
        }
        throw new IllegalStateException("Tipo de autenticação não suportado");
    }
}