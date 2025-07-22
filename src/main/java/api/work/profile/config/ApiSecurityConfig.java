package api.work.profile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Objects;

@Configuration
public class ApiSecurityConfig {
    
    @Value("${app.api.key:}")
    private String apiKey;
    
    public void validateApiKey(String providedKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BadCredentialsException("API key não configurada");
        }
        
        if (providedKey == null || providedKey.isEmpty()) {
            throw new BadCredentialsException("API key não fornecida");
        }
        
        if (!Objects.equals(apiKey, providedKey)) {
            throw new BadCredentialsException("API key inválida");
        }
    }
}