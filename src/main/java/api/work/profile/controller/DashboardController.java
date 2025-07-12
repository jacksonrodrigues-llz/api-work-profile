package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.DashboardService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final UserService userService;
    private final DashboardService dashboardService;
    
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
            User user = null;
            
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                user = userService.findOrCreateUser(oauth2User);
            } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User userDetails = 
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
                user = userService.findByEmail(userDetails.getUsername());
            }
            
            if (user == null) {
                log.error("[DASHBOARD] Usuário não encontrado: {}", authentication.getName());
                return "redirect:/login";
            }
            
            model.addAttribute("user", user);
            model.addAttribute("metrics", dashboardService.getDashboardMetrics(user));
            
            return "dashboard";
        } catch (Exception e) {
            log.error("[DASHBOARD] Erro para {}: {}", authentication.getName(), e.getMessage());
            model.addAttribute("errorMessage", "Erro ao carregar dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }
}