package api.work.profile.service;

import api.work.profile.config.RecaptchaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptchaService {
    
    private final RecaptchaConfig recaptchaConfig;
    private final RestTemplate restTemplate;
    
    public boolean validateRecaptcha(String recaptchaResponse) {
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }
        
        try {
            MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
            requestMap.add("secret", recaptchaConfig.getSecretKey());
            requestMap.add("response", recaptchaResponse);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                recaptchaConfig.getVerifyUrl(), 
                requestMap, 
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody == null) {
                return false;
            }
            
            return Boolean.TRUE.equals(responseBody.get("success"));
        } catch (Exception e) {
            log.error("Erro ao validar reCAPTCHA: {}", e.getMessage());
            return false;
        }
    }
    
    public String getSiteKey() {
        return recaptchaConfig.getSiteKey();
    }
}