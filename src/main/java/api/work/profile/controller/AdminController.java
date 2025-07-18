package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.service.AdminService;
import api.work.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final AdminService adminService;
    private final ProfileService profileService;
    
    @GetMapping("/users")
    public String listUsers(Authentication authentication,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        try {
            var currentUser = profileService.getUserFromAuthentication(authentication);
            adminService.validateAdminAccess(currentUser);
            
            var users = adminService.getAllUsers(currentUser, PageRequest.of(page, size));
            
            model.addAttribute("users", users);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", users.getTotalPages());
            model.addAttribute("user", currentUser);
            
            return "admin/users/list";
        } catch (SecurityException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/dashboard";
        }
    }
    
    @GetMapping("/users/new")
    public String newUser(Authentication authentication, Model model) {
        try {
            var currentUser = profileService.getUserFromAuthentication(authentication);
            adminService.validateAdminAccess(currentUser);
            
            model.addAttribute("user", User.builder().build());
            model.addAttribute("currentUser", currentUser);
            return "admin/users/form";
        } catch (SecurityException e) {
            return "redirect:/dashboard";
        }
    }
    
    @PostMapping("/users")
    public String saveUser(Authentication authentication, 
                          @ModelAttribute User user,
                          RedirectAttributes redirectAttributes) {
        try {
            var currentUser = profileService.getUserFromAuthentication(authentication);
            adminService.saveUser(user, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário salvo com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar usuário: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            var currentUser = profileService.getUserFromAuthentication(authentication);
            var user = adminService.getUserForEdit(id, currentUser);
            
            model.addAttribute("user", user);
            model.addAttribute("currentUser", currentUser);
            return "admin/users/form";
        } catch (SecurityException e) {
            return "redirect:/dashboard";
        }
    }
    
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            var currentUser = profileService.getUserFromAuthentication(authentication);
            var token = adminService.generatePasswordResetToken(id, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Token de reset gerado. Link: /reset-password?token=" + token);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao gerar token: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id, 
                                @RequestParam String role,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            var adminUser = profileService.getUserFromAuthentication(authentication);
            var newRole = UserRole.valueOf(role);
            
            adminService.updateUserRole(id, newRole, adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Role alterada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao alterar role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}