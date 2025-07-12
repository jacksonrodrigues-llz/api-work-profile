package api.work.profile.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, RedirectAttributes redirectAttributes, 
                                       jakarta.servlet.http.HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        
        // Ignorar erros de recursos estáticos comuns
        if (requestUri.contains("favicon.ico") || 
            requestUri.contains(".well-known") ||
            requestUri.contains("chrome-extension") ||
            requestUri.contains("devtools")) {
            return null; // Retorna 404 silenciosamente
        }
        
        log.error("[ERROR] {} - {}", requestUri, e.getMessage());
        
        // Log causa raiz apenas se for diferente
        if (e instanceof RuntimeException && e.getCause() != null && 
            !e.getMessage().equals(e.getCause().getMessage())) {
            log.error("[ERROR] Causa: {}", e.getCause().getMessage());
        }
        
        redirectAttributes.addFlashAttribute("errorMessage", 
            "Ocorreu um erro inesperado. Tente novamente.");
        redirectAttributes.addFlashAttribute("errorType", "danger");
        
        return "redirect:/dashboard";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        log.warn("[VALIDATION] {}", e.getMessage());
        
        redirectAttributes.addFlashAttribute("errorMessage", 
            "Dados inválidos: " + e.getMessage());
        redirectAttributes.addFlashAttribute("errorType", "warning");
        
        return "redirect:/dashboard";
    }
}