package api.work.profile.controller;

import api.work.profile.entity.Goal;
import api.work.profile.service.GoalService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
    
    private final GoalService goalService;
    private final ProfileService profileService;
    
    @GetMapping
    public String list(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var goalData = goalService.getGoalData(user);
        
        model.addAllAttributes(goalData);
        model.addAttribute("user", user);
        
        return "goals/list";
    }
    
    @GetMapping("/new")
    public String newGoal(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("goal", Goal.builder().build());
        model.addAttribute("user", user);
        return "goals/form";
    }
    
    @PostMapping
    public String save(Authentication authentication, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            goalService.saveGoal(goal, user);
            redirectAttributes.addFlashAttribute("successMessage", "Meta salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar meta: " + e.getMessage());
        }
        return "redirect:/goals";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var goal = goalService.getGoalForEdit(id, user);
        
        model.addAttribute("goal", goal);
        model.addAttribute("user", user);
        return "goals/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Goal goal, RedirectAttributes redirectAttributes) {
        try {
            goalService.updateGoal(id, goal);
            redirectAttributes.addFlashAttribute("successMessage", "Meta atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar meta: " + e.getMessage());
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
            goalService.updateGoalStatus(id, payload.get("status"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}