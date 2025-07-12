package api.work.profile.controller;

import api.work.profile.entity.Achievement;
import api.work.profile.entity.User;
import api.work.profile.repository.AchievementRepository;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
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
    public String list(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("achievements", achievementRepository.findByUserOrderByAchievedAtDesc(user));
        model.addAttribute("user", user);
        return "achievements/list";
    }
    
    @GetMapping("/new")
    public String newAchievement(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("achievement", new Achievement());
        model.addAttribute("user", user);
        return "achievements/form";
    }
    
    @PostMapping
    public String save(Authentication authentication, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);
            achievement.setUser(user);
            achievementRepository.save(achievement);
            
            log.info("[ACHIEVEMENT] Criada: {} (ID: {})", achievement.getTitle(), achievement.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Conquista salva com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[ACHIEVEMENT] Erro ao criar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar conquista: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/achievements";
    }
    
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        Achievement achievement = achievementRepository.findById(id).orElse(null);
        if (achievement == null) {
            return "redirect:/achievements";
        }
        model.addAttribute("achievement", achievement);
        model.addAttribute("user", user);
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
            log.info("[ACHIEVEMENT] Atualizada: {} (ID: {})", existing.getTitle(), id);
            
            redirectAttributes.addFlashAttribute("successMessage", "Conquista atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[ACHIEVEMENT] Erro ao atualizar ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar conquista: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/achievements";
    }
    
    @PostMapping("/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Achievement achievement, RedirectAttributes redirectAttributes) {
        return update(id, achievement, redirectAttributes);
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