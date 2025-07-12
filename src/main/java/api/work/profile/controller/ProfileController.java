package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
    public String profile(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findOrCreateUser(principal);
        model.addAttribute("user", user);
        
        // Lista de avatars tecnológicos
        String[] avatars = {
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev1",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev2",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev3",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev4",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev5",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev6",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev7",
            "https://api.dicebear.com/7.x/avataaars/svg?seed=dev8"
        };
        model.addAttribute("avatars", avatars);
        
        return "profile";
    }
    
    @PostMapping
    public String updateProfile(@AuthenticationPrincipal OAuth2User principal, 
                               @ModelAttribute User userForm, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findOrCreateUser(principal);
            user.setName(userForm.getName());
            user.setCompany(userForm.getCompany());
            user.setPosition(userForm.getPosition());
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar perfil: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar perfil: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/photo")
    public String uploadPhoto(@AuthenticationPrincipal OAuth2User principal,
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
            User user = userService.findOrCreateUser(principal);
            user.setProfilePhoto("/uploads/" + filename);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Foto atualizada com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (IOException e) {
            log.error("Erro ao fazer upload da foto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao fazer upload da foto");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/avatar")
    public String updateAvatar(@AuthenticationPrincipal OAuth2User principal,
                              @RequestParam("avatar") String avatar,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findOrCreateUser(principal);
            user.setAvatar(avatar);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Avatar atualizado com sucesso!");
            redirectAttributes.addFlashAttribute("messageType", "success");
            
        } catch (Exception e) {
            log.error("Erro ao atualizar avatar: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar avatar");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        
        return "redirect:/profile";
    }
}