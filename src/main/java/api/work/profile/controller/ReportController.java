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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    private final ProfileService profileService;
    
    @GetMapping({"", "/"})
    public String reports(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("user", user);
        return "reports/cards";
    }
    
    @GetMapping("/personal-metrics")
    public String personalMetrics(Authentication authentication, @RequestParam(required = false) String periodo, Model model) {
        try {
            var user = profileService.getUserFromAuthentication(authentication);
            log.debug("[REPORT] Carregando métricas pessoais para: {}", user.getEmail());
            
            var reportData = reportService.getReportData(user, periodo);
            
            model.addAttribute("user", user);
            model.addAttribute("currentUser", user);
            model.addAllAttributes(reportData);
            
            return "reports/personal-metrics";
        } catch (Exception e) {
            log.error("[REPORT] Erro ao carregar métricas pessoais: {}", e.getMessage());
            model.addAttribute("errorMessage", "Erro ao carregar relatórios: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/personal-metrics-data")
    @ResponseBody
    public Map<String, Object> getPersonalMetricsData(@RequestParam(required = false) String periodo, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        return reportService.getReportData(user, periodo);
    }
    
    @GetMapping("/dt")
    public String dtReport(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("user", user);
        return "reports/dt";
    }
    
    @GetMapping("/dt-data")
    @ResponseBody
    public Map<String, Object> getDtData(@RequestParam(required = false) String periodo, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        log.info("[DT_DATA] Solicitação de dados DT para usuário: {}, período: {}", user.getEmail(), periodo);
        var result = reportService.getDtReportData(user, periodo);
        log.info("[DT_DATA] Retornando dados: criticalOpen={}, resolved={}", result.get("criticalOpen"), result.get("resolved"));
        return result;
    }
    
    @GetMapping("/test-dt")
    @ResponseBody
    public Map<String, Object> testDtData(Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        log.info("[TEST_DT] Testando dados para usuário: {}", user.getEmail());
        return reportService.getDtReportData(user, null);
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