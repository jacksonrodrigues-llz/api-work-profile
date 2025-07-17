package api.work.profile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecaptchaConfig {
    
    @Value("${recaptcha.site-key}")
    private String siteKey;
    
    @Value("${recaptcha.secret-key}")
    private String secretKey;
    
    @Value("${recaptcha.verify-url}")
    private String verifyUrl;
    
    public String getSiteKey() {
        return siteKey;
    }
    
    public String getSecretKey() {
        return secretKey;
    }
    
    public String getVerifyUrl() {
        return verifyUrl;
    }
}