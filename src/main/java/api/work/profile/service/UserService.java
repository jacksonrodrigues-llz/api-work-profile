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
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String githubUsername = principal.getAttribute("login");
        String avatarUrl = principal.getAttribute("avatar_url");
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGithubUsername(githubUsername);
            user.setAvatar(avatarUrl);
            // Primeiro usuário se torna admin automaticamente
            long userCount = userRepository.count();
            if (userCount == 0) {
                user.setRole(UserRole.ADMIN);
                log.info("[AUTH] Primeiro usuário registrado como ADMIN: {}", email);
            } else {
                user.setRole(UserRole.USER);
            }
            user.setEnabled(true);
            user = userRepository.save(user);
            log.info("[AUTH] Novo usuário criado: {} (Role: {})", email, user.getRole());
            return user;
        }
        
        User existingUser = userOpt.get();
        // Atualizar informações do GitHub se necessário
        boolean needsUpdate = false;
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
        
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        user.setRole(newRole);
        log.info("[ADMIN] Role alterada: {} -> {} (por: {})", user.getEmail(), newRole, adminUser.getEmail());
        return userRepository.save(user);
    }
    
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
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