package api.work.profile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2Config {

    private String clientId = "Ov23li1tI16Hb3jbYl3c";
    private String clientSecret = "dc343ab563dfc795fa14cfab04d9d6356d28b0b3";

    @Value("${BASE_URL:https://api-work-profile.onrender.com}")
    private String baseUrl;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(githubClientRegistration());
    }

    private ClientRegistration githubClientRegistration() {
        return ClientRegistration.withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(baseUrl + "/login/oauth2/code/github")
                .scope("user:email", "repo")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("login")
                .clientName("GitHub")
                .build();
    }
}