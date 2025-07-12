package api.work.profile.controller;

import api.work.profile.entity.Goal;
import api.work.profile.entity.User;
import api.work.profile.repository.GoalRepository;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final UserService userService;
    
    @GetMapping
    public String list(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
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
        model.addAttribute("user", user);
        
        return "goals/list";
    }
    
    @GetMapping("/new")
    public String newGoal(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("goal", new Goal());
        model.addAttribute("user", user);
        return "goals/form";
    }
    
    @PostMapping
    public String save(Authentication authentication, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);
            goal.setUser(user);
            goalRepository.save(goal);
            
            log.info("[GOAL] Criada: {} (ID: {})", goal.getTitle(), goal.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Meta salva com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[GOAL] Erro ao criar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar meta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/goals";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        Goal goal = goalRepository.findById(id).orElse(null);
        if (goal == null) {
            return "redirect:/goals";
        }
        model.addAttribute("goal", goal);
        model.addAttribute("user", user);
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
            log.info("[GOAL] Atualizada: {} (ID: {})", existing.getTitle(), id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Meta atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[GOAL] Erro ao atualizar ID {}: {}", id, e.getMessage());
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
            log.debug("[GOAL] Status atualizado: ID {} -> {}", id, newStatus);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[GOAL] Erro ao atualizar status ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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