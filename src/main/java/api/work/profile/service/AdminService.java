package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public void validateAdminAccess(User user) {
        if (user == null) {
            throw new SecurityException("Usuário não encontrado");
        }
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("[ADMIN] Acesso negado para: {} (Role: {})", user.getEmail(), user.getRole());
            throw new SecurityException("Acesso negado - Apenas administradores");
        }
    }

    public Page<User> getAllUsers(User adminUser, Pageable pageable) {
        validateAdminAccess(adminUser);
        return userService.findAll(pageable);
    }

    public User saveUser(User user, User adminUser) {
        validateAdminAccess(adminUser);
        
        if (user.getId() != null) {
            // Edição - preservar dados existentes
            var existing = userService.findById(user.getId());
            var updated = existing.toBuilder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .category(user.getCategory())
                .enabled(user.getEnabled() != null ? user.getEnabled() : true)
                .build();
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                updated = updated.toBuilder()
                    .password(passwordEncoder.encode(user.getPassword()))
                    .build();
            }
            
            return userService.save(updated);
        } else {
            // Novo usuário
            var newUser = user.toBuilder()
                .enabled(user.getEnabled() != null ? user.getEnabled() : true)
                .build();
            
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                newUser = newUser.toBuilder()
                    .password(passwordEncoder.encode(user.getPassword()))
                    .build();
            }
            
            return userService.save(newUser);
        }
    }

    public User getUserForEdit(Long id, User adminUser) {
        validateAdminAccess(adminUser);
        return userService.findById(id);
    }

    public String generatePasswordResetToken(Long id, User adminUser) {
        validateAdminAccess(adminUser);
        
        var user = userService.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        var token = UUID.randomUUID().toString();
        var updated = user.toBuilder()
            .passwordResetToken(token)
            .tokenExpiration(LocalDateTime.now().plusHours(24))
            .build();
        
        userService.save(updated);
        return token;
    }

    public void updateUserRole(Long id, UserRole newRole, User adminUser) {
        validateAdminAccess(adminUser);
        userService.updateUserRole(id, newRole, adminUser);
    }
    
    public void disableUser(Long id, User adminUser) {
        validateAdminAccess(adminUser);
        
        var user = userService.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        if (user.getId().equals(adminUser.getId())) {
            throw new IllegalArgumentException("Não é possível desabilitar seu próprio usuário");
        }
        
        var updated = user.toBuilder()
            .active(false)
            .enabled(false)
            .build();
        
        userService.save(updated);
        log.info("[ADMIN] Usuário {} desabilitado por {}", user.getEmail(), adminUser.getEmail());
    }
    
    public void enableUser(Long id, User adminUser) {
        validateAdminAccess(adminUser);
        
        var user = userService.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        var updated = user.toBuilder()
            .active(true)
            .enabled(true)
            .build();
        
        userService.save(updated);
        log.info("[ADMIN] Usuário {} reabilitado por {}", user.getEmail(), adminUser.getEmail());
    }
    
    public void bulkDisableUsers(java.util.List<Long> userIds, User adminUser) {
        validateAdminAccess(adminUser);
        
        for (Long userId : userIds) {
            if (!userId.equals(adminUser.getId())) {
                disableUser(userId, adminUser);
            }
        }
        
        log.info("[ADMIN] {} usuários desabilitados em massa por {}", userIds.size(), adminUser.getEmail());
    }
}