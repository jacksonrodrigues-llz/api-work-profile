package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    
    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/";
    
    @GetMapping
    public String profile(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("user", user);
        
        // Lista de avatars militares
        String[] avatars = {
            "https://api.dicebear.com/7.x/personas/svg?seed=military1&backgroundColor=2c3e50",
            "https://api.dicebear.com/7.x/personas/svg?seed=military2&backgroundColor=34495e",
            "https://api.dicebear.com/7.x/personas/svg?seed=military3&backgroundColor=7f8c8d",
            "https://api.dicebear.com/7.x/personas/svg?seed=military4&backgroundColor=95a5a6",
            "https://api.dicebear.com/7.x/personas/svg?seed=military5&backgroundColor=2c3e50",
            "https://api.dicebear.com/7.x/personas/svg?seed=military6&backgroundColor=34495e",
            "https://api.dicebear.com/7.x/personas/svg?seed=military7&backgroundColor=7f8c8d",
            "https://api.dicebear.com/7.x/personas/svg?seed=military8&backgroundColor=95a5a6"
        };
        model.addAttribute("avatars", avatars);
        
        return "profile";
    }
    
    @PostMapping
    public String updateProfile(Authentication authentication, 
                               @ModelAttribute User userForm, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);
            user.setName(userForm.getName());
            user.setCompany(userForm.getCompany());
            user.setPosition(userForm.getPosition());
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[PROFILE] Erro ao atualizar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/photo")
    public String uploadPhoto(Authentication authentication,
                             @RequestParam("photo") MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Selecione uma foto para upload");
                return "redirect:/profile";
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get("src/main/resources/static/" + UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Update user profile
            User user = getUserFromAuthentication(authentication);
            user.setProfilePhoto("/uploads/" + filename);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Foto atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (IOException e) {
            log.error("[PROFILE] Erro no upload: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao fazer upload da foto");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/avatar")
    public String updateAvatar(Authentication authentication,
                              @RequestParam("avatar") String avatar,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = getUserFromAuthentication(authentication);
            user.setAvatar(avatar);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Avatar atualizado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("[PROFILE] Erro ao atualizar avatar: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar avatar");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
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