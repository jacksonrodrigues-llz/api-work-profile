package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String login = oauth2User.getAttribute("login");
        String avatarUrl = oauth2User.getAttribute("avatar_url");
        
        // Se email for null, usar login@github.com
        final String finalEmail = (email == null || email.isEmpty()) ? login + "@github.com" : email;
        
        // Se name for null, usar login
        final String finalName = (name == null || name.isEmpty()) ? login : name;
        
        User user = userRepository.findByEmail(finalEmail)
            .orElseGet(() -> createNewUser(finalEmail, finalName, login, avatarUrl));
        
        // Atualizar informações do GitHub
        user.setGithubUsername(login);
        user.setAvatar(avatarUrl);
        user.setAvatarUrl(avatarUrl);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("OAuth2 login: {} ({})", finalName, finalEmail);
        
        return oauth2User;
    }
    
    private User createNewUser(String email, String name, String login, String avatarUrl) {
        User user = User.builder()
            .email(email)
            .name(name)
            .githubUsername(login)
            .avatar(avatarUrl)
            .avatarUrl(avatarUrl)
            .role(UserRole.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .lastLogin(LocalDateTime.now())
            .build();
        
        return userRepository.save(user);
    }
}