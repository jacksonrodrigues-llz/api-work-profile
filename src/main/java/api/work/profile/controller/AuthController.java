package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", User.builder().build());
        model.addAttribute("recaptchaSiteKey", authService.getRecaptchaSiteKey());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                              @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaResponse,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        try {
            if (!authService.validateRecaptcha(recaptchaResponse)) {
                model.addAttribute("recaptchaSiteKey", authService.getRecaptchaSiteKey());
                model.addAttribute("errorMessage", "Por favor, confirme que você não é um robô");
                return "register";
            }
            
            authService.registerUser(user.getName(), user.getEmail(), user.getPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Conta criada com sucesso! Faça login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar conta: " + e.getMessage());
            return "redirect:/login";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model) {
        try {
            authService.validateResetToken(token);
            model.addAttribute("token", token);
            return "reset-password";
        } catch (Exception e) {
            model.addAttribute("error", "Token inválido ou expirado");
            return "reset-password";
        }
    }
    
    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, 
                                     @RequestParam String password,
                                     RedirectAttributes redirectAttributes) {
        try {
            authService.resetPassword(token, password);
            redirectAttributes.addFlashAttribute("successMessage", "Senha alterada com sucesso!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao alterar senha: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}