package api.work.profile.service;

import api.work.profile.entity.User;
import api.work.profile.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGithubUsername(githubUsername);
            user = userRepository.save(user);
            log.info("Novo usuário criado: {}", email);
            return user;
        }
        
        return userOpt.get();
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
}