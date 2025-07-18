package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RecaptchaService recaptchaService;

    public String getRecaptchaSiteKey() {
        return recaptchaService.getSiteKey();
    }

    public boolean validateRecaptcha(String recaptchaResponse) {
        return recaptchaResponse != null && !recaptchaResponse.isEmpty() && 
               recaptchaService.validateRecaptcha(recaptchaResponse);
    }

    public User registerUser(String name, String email, String password) {
        if (userService.findByEmail(email) != null) {
            throw new IllegalArgumentException("E-mail já cadastrado!");
        }

        var user = User.builder()
            .name(name)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(UserRole.USER)
            .enabled(true)
            .build();

        return userService.save(user);
    }

    public void validateResetToken(String token) {
        var user = userService.findByPasswordResetToken(token);
        if (user == null || user.getTokenExpiration() == null || 
            user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
    }

    public void resetPassword(String token, String password) {
        var user = userService.findByPasswordResetToken(token);
        if (user == null || user.getTokenExpiration() == null || 
            user.getTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }

        var updated = user.toBuilder()
            .password(passwordEncoder.encode(password))
            .passwordResetToken(null)
            .tokenExpiration(null)
            .build();

        userService.save(updated);
    }
}