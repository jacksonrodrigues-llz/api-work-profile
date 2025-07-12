package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.DashboardService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
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
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findOrCreateUser(principal);
        
        model.addAttribute("user", user);
        model.addAttribute("metrics", dashboardService.getDashboardMetrics(user));
        
        return "dashboard";
    }
}