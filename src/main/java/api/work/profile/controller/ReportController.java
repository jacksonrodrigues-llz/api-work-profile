package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.repository.UserRepository;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final GoalRepository goalRepository;
    private final AchievementRepository achievementRepository;
    
    @GetMapping
    public String reports(@AuthenticationPrincipal OAuth2User principal, Model model) {
        try {
            User user = getUser(principal);
            log.info("Gerando relatório para usuário: {}", user.getEmail());
            
            model.addAttribute("user", user);
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
            log.error("Erro ao gerar relatório: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Erro ao carregar relatórios: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);
        byte[] pdfBytes = reportService.generatePdfReport(user);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-profissional.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    private User getUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String githubUsername = principal.getAttribute("login");
        
        if (email == null || email.isEmpty()) {
            return userRepository.findByGithubUsername(githubUsername).orElseThrow();
        }
        return userRepository.findByEmail(email).orElseThrow();
    }
}