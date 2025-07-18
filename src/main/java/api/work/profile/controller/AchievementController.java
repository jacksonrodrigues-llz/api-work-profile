package api.work.profile.controller;

import api.work.profile.entity.Achievement;
import api.work.profile.service.AchievementService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {
    
    private final AchievementService achievementService;
    private final ProfileService profileService;
    
    @GetMapping
    public String list(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("achievements", achievementService.getAchievementsByUser(user));
        model.addAttribute("user", user);
        return "achievements/list";
    }
    
    @GetMapping("/new")
    public String newAchievement(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("achievement", Achievement.builder().build());
        model.addAttribute("user", user);
        return "achievements/form";
    }
    
    @PostMapping
    public String save(Authentication authentication, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            achievementService.saveAchievement(achievement, user);
            redirectAttributes.addFlashAttribute("successMessage", "Conquista salva com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar conquista: " + e.getMessage());
        }
        return "redirect:/achievements";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var achievement = achievementService.getAchievementForEdit(id, user);
        
        model.addAttribute("achievement", achievement);
        model.addAttribute("user", user);
        return "achievements/form";
    }
    
    @PutMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        try {
            achievementService.updateAchievement(id, achievement);
            redirectAttributes.addFlashAttribute("successMessage", "Conquista atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar conquista: " + e.getMessage());
        }
        return "redirect:/achievements";
    }
    
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        return update(id, achievement, redirectAttributes);
    }
}