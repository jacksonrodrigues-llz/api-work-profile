package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.enums.UserRole;
import api.work.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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
        try {
            log.info("[OAUTH2] Iniciando autenticação GitHub...");
            log.debug("[OAUTH2] Client Registration: {}", userRequest.getClientRegistration().getClientId());
            log.debug("[OAUTH2] Access Token: {}", userRequest.getAccessToken().getTokenValue().substring(0, 10) + "...");
            
            OAuth2User oauth2User = super.loadUser(userRequest);
            
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String login = oauth2User.getAttribute("login");
            String avatarUrl = oauth2User.getAttribute("avatar_url");
            
            log.info("[OAUTH2] Dados recebidos - Login: {}, Email: {}, Name: {}", login, email, name);
            log.debug("[OAUTH2] Todos os atributos: {}", oauth2User.getAttributes());
            
            if (login == null || login.trim().isEmpty()) {
                log.error("[OAUTH2] Login do GitHub é obrigatório mas não foi fornecido");
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info", "Login do GitHub não encontrado", null));
            }
            
            // Se email for null, usar login@github.com
            final String finalEmail = (email == null || email.isEmpty()) ? login + "@github.com" : email;
            
            // Se name for null, usar login
            final String finalName = (name == null || name.isEmpty()) ? login : name;
            
            log.info("[OAUTH2] Processando usuário: {} ({})", finalName, finalEmail);
            
            User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> {
                    log.info("[OAUTH2] Criando novo usuário: {}", finalEmail);
                    return createNewUser(finalEmail, finalName, login, avatarUrl);
                });
            
            // Atualizar informações do GitHub
            user.setGithubUsername(login);
            user.setAvatar(avatarUrl);
            user.setAvatarUrl(avatarUrl);
            user.setLastLogin(LocalDateTime.now());
            user.setActive(true);
            user.setEnabled(true);
            userRepository.save(user);
            
            log.info("[OAUTH2] Login realizado com sucesso: {} (ID: {})", finalName, user.getId());
            
            return oauth2User;
            
        } catch (OAuth2AuthenticationException e) {
            log.error("[OAUTH2] Erro OAuth2: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[OAUTH2] Erro durante autenticação GitHub: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("authentication_error", "Erro na autenticação GitHub: " + e.getMessage(), null), e);
        }
    }
    
    private User createNewUser(String email, String name, String login, String avatarUrl) {
        try {
            User user = User.builder()
                .email(email)
                .name(name)
                .githubUsername(login)
                .avatar(avatarUrl)
                .avatarUrl(avatarUrl)
                .role(UserRole.USER)
                .enabled(true)
                .active(true)
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
            
            User savedUser = userRepository.save(user);
            log.info("[OAUTH2] Novo usuário criado com sucesso: {} (ID: {})", email, savedUser.getId());
            return savedUser;
            
        } catch (Exception e) {
            log.error("[OAUTH2] Erro ao criar novo usuário {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Erro ao criar usuário: " + e.getMessage(), e);
        }
    }
}