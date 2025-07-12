package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/users")
    public String listUsers(Authentication authentication,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Model model) {
        User currentUser = getUserFromAuthentication(authentication);
        
        if (currentUser == null) {
            log.error("[ADMIN] Usuário não encontrado");
            model.addAttribute("errorMessage", "Usuário não encontrado");
            return "error";
        }
        
        if (currentUser.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            log.warn("[ADMIN] Acesso negado para: {} (Role: {})", currentUser.getEmail(), currentUser.getRole());
            model.addAttribute("errorMessage", "Acesso negado - Apenas administradores");
            return "redirect:/dashboard";
        }
        
        Page<User> users = userService.findAll(PageRequest.of(page, size));
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("user", currentUser);
        
        return "admin/users/list";
    }
    
    @GetMapping("/users/new")
    public String newUser(Authentication authentication, Model model) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null || currentUser.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        model.addAttribute("currentUser", currentUser);
        return "admin/users/form";
    }
    
    @PostMapping("/users")
    public String saveUser(Authentication authentication, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null || currentUser.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            return "redirect:/dashboard";
        }
        try {
            if (user.getId() != null) {
                // Edição - preservar dados existentes
                User existing = userService.findById(user.getId());
                existing.setName(user.getName());
                existing.setEmail(user.getEmail());
                existing.setRole(user.getRole());
                existing.setEnabled(user.getEnabled() != null ? user.getEnabled() : true);
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    existing.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                userService.save(existing);
            } else {
                // Novo usuário
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                }
                if (user.getEnabled() == null) user.setEnabled(true);
                userService.save(user);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Usuário salvo com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar usuário: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Authentication authentication, Model model) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null || currentUser.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            return "redirect:/dashboard";
        }
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        return "admin/users/form";
    }
    
    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User currentUser = getUserFromAuthentication(authentication);
        if (currentUser == null || currentUser.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            return "redirect:/dashboard";
        }
        try {
            User user = userService.findById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Usuário não encontrado");
                return "redirect:/admin/users";
            }
            
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setTokenExpiration(LocalDateTime.now().plusHours(24));
            userService.save(user);
            
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
            User adminUser = getUserFromAuthentication(authentication);
            api.work.profile.enums.UserRole newRole = api.work.profile.enums.UserRole.valueOf(role);
            
            userService.updateUserRole(id, newRole, adminUser);
            redirectAttributes.addFlashAttribute("successMessage", "Role alterada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao alterar role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            org.springframework.security.oauth2.core.user.OAuth2User oauth2User = 
                (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
            return userService.findOrCreateUser(oauth2User);
        } 
        
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return userService.findByEmail(userDetails.getUsername());
        }
        
        log.error("[ADMIN] Tipo de autenticação não suportado: {}", authentication.getPrincipal().getClass());
        return null;
    }
}