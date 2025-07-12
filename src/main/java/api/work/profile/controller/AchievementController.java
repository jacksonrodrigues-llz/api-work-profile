package api.work.profile.controller;

import api.work.profile.entity.Achievement;
import api.work.profile.entity.User;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/achievements")
@RequiredArgsConstructor
@Slf4j
public class AchievementController {
    
    private final AchievementRepository achievementRepository;
    private final UserService userService;
    
    @GetMapping
    public String list(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findOrCreateUser(principal);
        model.addAttribute("achievements", achievementRepository.findByUserOrderByAchievedAtDesc(user));
        return "achievements/list";
    }
    
    @GetMapping("/new")
    public String newAchievement(Model model) {
        model.addAttribute("achievement", new Achievement());
        return "achievements/form";
    }
    
    @PostMapping
    public String save(@AuthenticationPrincipal OAuth2User principal, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findOrCreateUser(principal);
            achievement.setUser(user);
            achievementRepository.save(achievement);
            
            log.info("Conquista salva com sucesso: {} para usuário: {}", achievement.getTitle(), user.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Conquista salva com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao salvar conquista: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar conquista: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/achievements";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Achievement achievement = achievementRepository.findById(id).orElse(null);
        if (achievement == null) {
            return "redirect:/achievements";
        }
        model.addAttribute("achievement", achievement);
        return "achievements/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        try {
            Achievement existing = achievementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conquista não encontrada com ID: " + id));
            
            existing.setTitle(achievement.getTitle());
            existing.setDescription(achievement.getDescription());
            existing.setType(achievement.getType());
            existing.setImpact(achievement.getImpact());
            existing.setRecognition(achievement.getRecognition());
            existing.setAchievedAt(achievement.getAchievedAt());
            
            achievementRepository.save(existing);
            log.info("Conquista atualizada com sucesso: ID {}", id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Conquista atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar conquista ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar conquista: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/achievements";
    }
    
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        return update(id, achievement, redirectAttributes);
    }
    

}