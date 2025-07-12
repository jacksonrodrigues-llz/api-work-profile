package api.work.profile.controller;

import api.work.profile.entity.Activity;
import api.work.profile.entity.User;
import api.work.profile.repository.ActivityRepository;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {
    
    private final ActivityRepository activityRepository;
    private final UserService userService;
    
    @GetMapping
    public String list(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findOrCreateUser(principal);
        var activities = activityRepository.findByUserOrderByCreatedAtDesc(user);
        
        // Separar atividades por status
        var todoActivities = activities.stream().filter(a -> a.getStatus() == Activity.ActivityStatus.TODO).toList();
        var inProgressActivities = activities.stream().filter(a -> a.getStatus() == Activity.ActivityStatus.IN_PROGRESS).toList();
        var testActivities = activities.stream().filter(a -> a.getStatus() == Activity.ActivityStatus.TEST).toList();
        var deployActivities = activities.stream().filter(a -> a.getStatus() == Activity.ActivityStatus.DEPLOY).toList();
        var doneActivities = activities.stream().filter(a -> a.getStatus() == Activity.ActivityStatus.DONE).toList();
        
        model.addAttribute("activities", activities);
        model.addAttribute("todoActivities", todoActivities);
        model.addAttribute("inProgressActivities", inProgressActivities);
        model.addAttribute("testActivities", testActivities);
        model.addAttribute("deployActivities", deployActivities);
        model.addAttribute("doneActivities", doneActivities);
        
        // Contadores
        model.addAttribute("todoCount", todoActivities.size());
        model.addAttribute("inProgressCount", inProgressActivities.size());
        model.addAttribute("testCount", testActivities.size());
        model.addAttribute("deployCount", deployActivities.size());
        model.addAttribute("doneCount", doneActivities.size());
        model.addAttribute("user", user);
        
        return "activities/list";
    }
    
    @GetMapping("/new")
    public String newActivity(Model model) {
        model.addAttribute("activity", new Activity());
        return "activities/form";
    }
    
    @PostMapping
    public String save(@AuthenticationPrincipal OAuth2User principal, @ModelAttribute Activity activity, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findOrCreateUser(principal);
            activity.setUser(user);
            
            if (activity.getStatus() == Activity.ActivityStatus.DONE && activity.getCompletedAt() == null) {
                activity.setCompletedAt(LocalDateTime.now());
            }
            
            activityRepository.save(activity);
            log.info("Atividade salva com sucesso: {} para usuário: {}", activity.getTitle(), user.getEmail());
            
            redirectAttributes.addFlashAttribute("successMessage", "Atividade salva com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao salvar atividade: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar atividade: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/activities";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Activity activity = activityRepository.findById(id).orElse(null);
        if (activity == null) {
            return "redirect:/activities";
        }
        model.addAttribute("activity", activity);
        return "activities/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Activity activity, RedirectAttributes redirectAttributes) {
        try {
            Activity existing = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada com ID: " + id));
            
            existing.setTitle(activity.getTitle());
            existing.setDescription(activity.getDescription());
            existing.setStatus(activity.getStatus());
            existing.setPriority(activity.getPriority());
            existing.setProject(activity.getProject());
            existing.setSkills(activity.getSkills());
            existing.setEstimatedHours(activity.getEstimatedHours());
            existing.setActualHours(activity.getActualHours());
            
            if (activity.getStatus() == Activity.ActivityStatus.DONE && existing.getCompletedAt() == null) {
                existing.setCompletedAt(LocalDateTime.now());
            }
            
            activityRepository.save(existing);
            log.info("Atividade atualizada com sucesso: ID {}", id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Atividade atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar atividade ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar atividade: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
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
            Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Atividade não encontrada"));
            
            String newStatus = payload.get("status");
            activity.setStatus(Activity.ActivityStatus.valueOf(newStatus));
            
            if (activity.getStatus() == Activity.ActivityStatus.DONE && activity.getCompletedAt() == null) {
                activity.setCompletedAt(LocalDateTime.now());
            }
            
            activityRepository.save(activity);
            log.info("Status da atividade {} atualizado para {}", id, newStatus);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar status da atividade {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    

}