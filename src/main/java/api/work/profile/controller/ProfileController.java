package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final ProfileService profileService;
    
    @GetMapping
    public String profile(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var avatars = new String[]{
            "https://api.dicebear.com/7.x/personas/svg?seed=military1&backgroundColor=2c3e50",
            "https://api.dicebear.com/7.x/personas/svg?seed=military2&backgroundColor=34495e",
            "https://api.dicebear.com/7.x/personas/svg?seed=military3&backgroundColor=7f8c8d",
            "https://api.dicebear.com/7.x/personas/svg?seed=military4&backgroundColor=95a5a6",
            "https://api.dicebear.com/7.x/personas/svg?seed=military5&backgroundColor=2c3e50",
            "https://api.dicebear.com/7.x/personas/svg?seed=military6&backgroundColor=34495e",
            "https://api.dicebear.com/7.x/personas/svg?seed=military7&backgroundColor=7f8c8d",
            "https://api.dicebear.com/7.x/personas/svg?seed=military8&backgroundColor=95a5a6"
        };
        
        model.addAttribute("user", user);
        model.addAttribute("avatars", avatars);
        return "profile";
    }
    
    @PostMapping
    public String updateProfile(Authentication authentication, 
                               @ModelAttribute User userForm,
                               RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            profileService.updateProfile(user, userForm);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
    @PostMapping("/photo")
    public String uploadPhoto(Authentication authentication,
                             @RequestParam("photo") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            profileService.uploadProfilePhoto(user, file);
            redirectAttributes.addFlashAttribute("successMessage", "Foto atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao fazer upload da foto");
        }
        return "redirect:/profile";
    }
    
    @PostMapping("/avatar")
    public String updateAvatar(Authentication authentication,
                              @RequestParam("avatar") String avatar,
                              RedirectAttributes redirectAttributes) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            profileService.updateAvatar(user, avatar);
            redirectAttributes.addFlashAttribute("successMessage", "Avatar atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar avatar");
        }
        return "redirect:/profile";
    }
}