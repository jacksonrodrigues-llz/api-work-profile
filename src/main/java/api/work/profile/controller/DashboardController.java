package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.repository.UserRepository;
import api.work.profile.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserRepository userRepository;
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
        User user = getOrCreateUser(principal);
        
        model.addAttribute("user", user);
        model.addAttribute("metrics", dashboardService.getDashboardMetrics(user));
        
        return "dashboard";
    }
    
    private User getOrCreateUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        String githubUsername = principal.getAttribute("login");
        
        // Se email não estiver disponível, usar GitHub username como identificador
        if (email == null || email.isEmpty()) {
            return userRepository.findByGithubUsername(githubUsername)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(githubUsername + "@github.local"); // Email temporário
                    newUser.setName(principal.getAttribute("name"));
                    newUser.setGithubUsername(githubUsername);
                    // Armazenar informações adicionais do GitHub
                    newUser.setCompany(principal.getAttribute("company"));
                    return userRepository.save(newUser);
                });
        }
        
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(principal.getAttribute("name"));
                newUser.setGithubUsername(githubUsername);
                newUser.setCompany(principal.getAttribute("company"));
                return userRepository.save(newUser);
            });
    }
}