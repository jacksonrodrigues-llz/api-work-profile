package api.work.profile.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Configuração para testes unitários.
 * Esta classe fornece beans mockados para evitar dependências externas durante os testes.
 */
@TestConfiguration
public class TestConfig {
    
    /**
     * Fornece um mock do ClientRegistrationRepository para evitar problemas com OAuth durante os testes.
     */
    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        return Mockito.mock(ClientRegistrationRepository.class);
    }
}