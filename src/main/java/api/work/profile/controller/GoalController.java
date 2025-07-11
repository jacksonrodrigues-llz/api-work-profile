package api.work.profile.controller;

import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import api.work.profile.repository.GoalRepository;
import api.work.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/goals")
@RequiredArgsConstructor
@Slf4j
public class GoalController {
    
    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    
    @GetMapping
    public String list(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = getUser(principal);
        var goals = goalRepository.findByUserOrderByCreatedAtDesc(user);
        
        // Separar metas por status
        var activeGoals = goals.stream().filter(g -> g.getStatus() == Goal.GoalStatus.ACTIVE).toList();
        var completedGoals = goals.stream().filter(g -> g.getStatus() == Goal.GoalStatus.COMPLETED).toList();
        var pausedGoals = goals.stream().filter(g -> g.getStatus() == Goal.GoalStatus.PAUSED).toList();
        var cancelledGoals = goals.stream().filter(g -> g.getStatus() == Goal.GoalStatus.CANCELLED).toList();
        
        model.addAttribute("goals", goals);
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("completedGoals", completedGoals);
        model.addAttribute("pausedGoals", pausedGoals);
        model.addAttribute("cancelledGoals", cancelledGoals);
        
        // Contadores
        model.addAttribute("activeCount", activeGoals.size());
        model.addAttribute("completedCount", completedGoals.size());
        model.addAttribute("pausedCount", pausedGoals.size());
        model.addAttribute("cancelledCount", cancelledGoals.size());
        
        return "goals/list";
    }
    
    @GetMapping("/new")
    public String newGoal(Model model) {
        model.addAttribute("goal", new Goal());
        return "goals/form";
    }
    
    @PostMapping
    public String save(@AuthenticationPrincipal OAuth2User principal, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        try {
            User user = getUser(principal);
            goal.setUser(user);
            goalRepository.save(goal);
            
            log.info("Meta salva com sucesso: {} para usuário: {}", goal.getTitle(), user.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Meta salva com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao salvar meta: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar meta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/goals";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Goal goal = goalRepository.findById(id).orElseThrow();
        model.addAttribute("goal", goal);
        return "goals/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        try {
            Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada com ID: " + id));
            
            existing.setTitle(goal.getTitle());
            existing.setDescription(goal.getDescription());
            existing.setTargetDate(goal.getTargetDate());
            existing.setProgressPercentage(goal.getProgressPercentage());
            existing.setStatus(goal.getStatus());
            existing.setCategory(goal.getCategory());
            
            goalRepository.save(existing);
            log.info("Meta atualizada com sucesso: ID {}", id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Meta atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar meta ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar meta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/goals";
    }
    
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        return update(id, goal, redirectAttributes);
    }
    
    @PatchMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meta não encontrada"));
            
            String newStatus = payload.get("status");
            goal.setStatus(Goal.GoalStatus.valueOf(newStatus));
            
            if (goal.getStatus() == Goal.GoalStatus.COMPLETED && goal.getCompletedAt() == null) {
                goal.setCompletedAt(LocalDateTime.now());
                goal.setProgressPercentage(100);
            }
            
            goalRepository.save(goal);
            log.info("Status da meta {} atualizado para {}", id, newStatus);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar status da meta {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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