package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;

    public User findOrCreateUser(OAuth2User principal) {
        var email = principal.<String>getAttribute("email");
        var name = principal.<String>getAttribute("name");
        var githubUsername = principal.<String>getAttribute("login");
        var avatarUrl = principal.<String>getAttribute("avatar_url");
        
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            var userCount = userRepository.count();
            var role = userCount == 0 ? UserRole.ADMIN : UserRole.USER;
            
            var user = User.builder()
                .email(email)
                .name(name)
                .githubUsername(githubUsername)
                .avatar(avatarUrl)
                .role(role)
                .enabled(true)
                .build();
            
            var saved = userRepository.save(user);
            log.info("[AUTH] Novo usuário criado: {} (Role: {})", email, saved.getRole());
            return saved;
        }
        
        var existingUser = userOpt.get();
        var needsUpdate = false;
        
        if (githubUsername != null && !githubUsername.equals(existingUser.getGithubUsername())) {
            existingUser.setGithubUsername(githubUsername);
            needsUpdate = true;
        }
        if (avatarUrl != null && !avatarUrl.equals(existingUser.getAvatar())) {
            existingUser.setAvatar(avatarUrl);
            needsUpdate = true;
        }
        
        if (needsUpdate) {
            existingUser = userRepository.save(existingUser);
            log.debug("[AUTH] Dados do GitHub atualizados para: {}", email);
        }
        return existingUser;
    }

    public User updateUserRole(Long userId, UserRole newRole, User adminUser) {
        if (adminUser.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Apenas administradores podem alterar roles");
        }
        
        var user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        var updated = user.toBuilder()
            .role(newRole)
            .build();
        log.info("[ADMIN] Role alterada: {} -> {} (por: {})", user.getEmail(), newRole, adminUser.getEmail());
        return userRepository.save(updated);
    }
    
    public User findByEmail(String email) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("[AUTH] Usuário não encontrado: {}", email);
        }
        return user;
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token).orElse(null);
    }
}