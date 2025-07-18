package api.work.profile.controller;

import api.work.profile.service.ProfileService;
import api.work.profile.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    private final ProfileService profileService;
    
    @GetMapping
    public String reports(Authentication authentication, Model model) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            log.debug("[REPORT] Carregando relatórios para: {}", user.getEmail());
            
            var reportData = reportService.getReportData(user);
            
            model.addAttribute("user", user);
            model.addAttribute("currentUser", user);
            model.addAllAttributes(reportData);
            
            return "reports/index";
        } catch (Exception e) {
            log.error("[REPORT] Erro ao carregar relatórios: {}", e.getMessage());
            model.addAttribute("errorMessage", "Erro ao carregar relatórios: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var pdfBytes = reportService.generatePdfReport(user);
        
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-profissional.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}