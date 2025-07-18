package api.work.profile.service;

import api.work.profile.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserService userService;
    private static final String UPLOAD_DIR = "uploads/";

    public User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            return userService.findOrCreateUser((OAuth2User) authentication.getPrincipal());
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userService.findByEmail(userDetails.getUsername());
        }
        throw new IllegalStateException("Tipo de autenticação não suportado");
    }

    public User updateProfile(User currentUser, User userForm) {
        var updated = currentUser.toBuilder()
            .name(userForm.getName())
            .company(userForm.getCompany())
            .position(userForm.getPosition())
            .category(userForm.getCategory())
            .build();
        
        return userService.save(updated);
    }

    public User updateAvatar(User user, String avatarUrl) {
        var updated = user.toBuilder()
            .avatar(avatarUrl)
            .build();
        return userService.save(updated);
    }

    public User uploadProfilePhoto(User user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
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
        var updated = user.toBuilder()
            .profilePhoto("/uploads/" + filename)
            .build();
        return userService.save(updated);
    }
}