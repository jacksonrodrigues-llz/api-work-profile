package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String name, 
                              @RequestParam String email, 
                              @RequestParam String password, 
                              RedirectAttributes redirectAttributes) {
        try {
            // Validar se usuário já existe
            if (userService.findByEmail(email) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "E-mail já cadastrado!");
                return "redirect:/login";
            }
            
            // Criar novo usuário
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(api.work.profile.enums.UserRole.USER);
            user.setEnabled(true);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Conta criada com sucesso! Faça login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar conta: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        User user = userService.findByPasswordResetToken(token);
        if (user == null || user.getTokenExpiration() == null || user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Token inválido ou expirado");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, 
                                     @RequestParam String password,
                                     RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByPasswordResetToken(token);
            if (user == null || user.getTokenExpiration() == null || user.getTokenExpiration().isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Token inválido ou expirado");
                return "redirect:/reset-password?token=" + token;
            }
            
            user.setPassword(passwordEncoder.encode(password));
            user.setPasswordResetToken(null);
            user.setTokenExpiration(null);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Senha alterada com sucesso!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao alterar senha: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}