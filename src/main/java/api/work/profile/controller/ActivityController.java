package api.work.profile.controller;

import api.work.profile.entity.Activity;
import api.work.profile.service.ActivityService;
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
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    private final ProfileService profileService;
    
    @GetMapping
    public String list(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var kanbanData = activityService.getKanbanData(user);
        
        model.addAllAttributes(kanbanData);
        model.addAttribute("user", user);
        
        return "activities/list";
    }
    
    @GetMapping("/new")
    public String newActivity(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("activity", Activity.builder().build());
        model.addAttribute("user", user);
        return "activities/form";
    }
    
    @PostMapping
    public String save(Authentication authentication, @ModelAttribute Activity activity, RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            activityService.saveActivity(activity, user);
            redirectAttributes.addFlashAttribute("successMessage", "Atividade salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar atividade: " + e.getMessage());
        }
        return "redirect:/activities";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var activity = activityService.getActivityForEdit(id, user);
        
        model.addAttribute("activity", activity);
        model.addAttribute("user", user);
        return "activities/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Activity activity, RedirectAttributes redirectAttributes) {
        try {
            activityService.updateActivity(id, activity);
            redirectAttributes.addFlashAttribute("successMessage", "Atividade atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar atividade: " + e.getMessage());
        }
        return "redirect:/activities";
    }
    
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Activity activity, RedirectAttributes redirectAttributes) {
        return update(id, activity, redirectAttributes);
    }
    
    @PatchMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            activityService.updateActivityStatus(id, payload.get("status"));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}