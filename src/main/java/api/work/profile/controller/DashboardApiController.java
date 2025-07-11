package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.repository.UserRepository;
import api.work.profile.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {
    
    private final DashboardService dashboardService;
    private final UserRepository userRepository;
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(@AuthenticationPrincipal OAuth2User principal) {
        User user = getUser(principal);
        Map<String, Object> metrics = dashboardService.getDashboardMetrics(user);
        return ResponseEntity.ok(metrics);
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