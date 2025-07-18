package api.work.profile.controller;

import api.work.profile.service.DashboardService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {
    
    private final DashboardService dashboardService;
    private final ProfileService profileService;
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics(Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var metrics = dashboardService.getDashboardMetrics(user);
        return ResponseEntity.ok(metrics);
    }
}