package api.work.profile.controller;

import api.work.profile.dto.TechDebtDTO;
import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.service.TechDebtService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tech-debts")
@RequiredArgsConstructor
public class TechDebtApiController {
    
    private final TechDebtService techDebtService;
    private final UserService userService;
    
    @PostMapping("/import")
    public ResponseEntity<?> importFromWebhook(@RequestBody TechDebtDTO dto) {
        try {
            // Para webhook, criar usuário genérico ou usar o criadoPor
            User user = userService.findByEmail(dto.getCriadoPor() + "@webhook.com");
            if (user == null) {
                user = new User();
                user.setEmail(dto.getCriadoPor() + "@webhook.com");
                user.setName(dto.getCriadoPor());
                user = userService.save(user);
            }
            
            TechDebt debt = dto.toEntity();
            debt.setUser(user);
            techDebtService.save(debt);
            
            return ResponseEntity.ok().body("Débito técnico importado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/json")
    public ResponseEntity<?> uploadJson(@RequestBody List<TechDebtDTO> dtos,
                                       @AuthenticationPrincipal OAuth2User principal) {
        try {
            User user = userService.findOrCreateUser(principal);
            int count = 0;
            for (TechDebtDTO dto : dtos) {
                TechDebt debt = dto.toEntity();
                debt.setUser(user);
                if (debt.getCriadoPor() == null || debt.getCriadoPor().isEmpty()) {
                    debt.setCriadoPor(user.getName());
                }
                techDebtService.save(debt);
                count++;
            }
            return ResponseEntity.ok().body("Importados " + count + " débitos técnicos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar JSON: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/single")
    public ResponseEntity<?> uploadSingle(@RequestBody TechDebtDTO dto,
                                         @AuthenticationPrincipal OAuth2User principal) {
        try {
            User user = userService.findOrCreateUser(principal);
            TechDebt debt = dto.toEntity();
            debt.setUser(user);
            if (debt.getCriadoPor() == null || debt.getCriadoPor().isEmpty()) {
                debt.setCriadoPor(user.getName());
            }
            techDebtService.save(debt);
            return ResponseEntity.ok().body("Débito técnico importado com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/sheet")
    public ResponseEntity<?> uploadSheet(@RequestParam("file") MultipartFile file,
                                        @AuthenticationPrincipal OAuth2User principal) {
        try {
            User user = userService.findOrCreateUser(principal);
            List<TechDebt> debts = techDebtService.importFromCsv(file, user);
            return ResponseEntity.ok().body("Importados " + debts.size() + " débitos técnicos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar planilha: " + e.getMessage());
        }
    }
}