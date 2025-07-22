package api.work.profile.controller;

import api.work.profile.dto.TechDebtDTO;
import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.service.TechDebtService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tech-debts")
@RequiredArgsConstructor
public class TechDebtApiController {
    
    private final TechDebtService techDebtService;
    private final UserService userService;
    private final api.work.profile.config.ApiSecurityConfig apiSecurityConfig;
    
    @PostMapping("/webhook")
    public ResponseEntity<?> importFromWebhook(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody TechDebtDTO dto) {
        try {
            apiSecurityConfig.validateApiKey(apiKey);
            
            if (dto.getCriadoPor() == null || dto.getCriadoPor().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Campo 'criadoPor' é obrigatório");
            }
            
            // Sanitizar entrada
            dto.setCriadoPor(dto.getCriadoPor().trim());
            
            // Para webhook, usar usuário sistema
            User systemUser = userService.findByEmail("system@webhook.internal");
            if (systemUser == null) {
                return ResponseEntity.badRequest().body("Usuário sistema não configurado");
            }
            
            TechDebt debt = dto.toEntity();
            debt.setUser(systemUser);
            techDebtService.save(debt);
            
            return ResponseEntity.ok().body("Débito técnico importado com sucesso");
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body("Não autorizado");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar solicitação");
        }
    }
    
    @PostMapping("/upload/single")
    public ResponseEntity<?> uploadSingle(@RequestBody TechDebtDTO dto,
                                         Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
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
    
    @PostMapping("/upload/json")
    public ResponseEntity<?> uploadJson(@RequestBody Object jsonData,
                                       Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            int count = 0;
            
            if (jsonData instanceof List) {
                // Lista de objetos
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonData;
                for (Map<String, Object> data : dataList) {
                    TechDebtDTO dto = mapToDto(data);
                    TechDebt debt = dto.toEntity();
                    debt.setUser(user);
                    if (debt.getCriadoPor() == null || debt.getCriadoPor().isEmpty()) {
                        debt.setCriadoPor(user.getName());
                    }
                    techDebtService.save(debt);
                    count++;
                }
            } else {
                // Objeto único
                Map<String, Object> data = (Map<String, Object>) jsonData;
                TechDebtDTO dto = mapToDto(data);
                TechDebt debt = dto.toEntity();
                debt.setUser(user);
                if (debt.getCriadoPor() == null || debt.getCriadoPor().isEmpty()) {
                    debt.setCriadoPor(user.getName());
                }
                techDebtService.save(debt);
                count = 1;
            }
            
            return ResponseEntity.ok().body("Importados " + count + " débitos técnicos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar JSON: " + e.getMessage());
        }
    }
    
    private TechDebtDTO mapToDto(Map<String, Object> data) {
        TechDebtDTO dto = new TechDebtDTO();
        dto.setProblema((String) data.get("problema"));
        dto.setDescricao((String) data.get("descricao"));
        
        // Obter e validar a prioridade
        Integer prioridade = null;
        try {
            if (data.get("prioridade") instanceof Integer) {
                prioridade = (Integer) data.get("prioridade");
            } else if (data.get("prioridade") instanceof Number) {
                prioridade = ((Number) data.get("prioridade")).intValue();
            } else if (data.get("prioridade") instanceof String) {
                prioridade = Integer.parseInt((String) data.get("prioridade"));
            }
            
            // Garantir que a prioridade esteja entre 1 e 4
            if (prioridade == null || prioridade < 1 || prioridade > 4) {
                prioridade = 1; // Valor padrão seguro
            }
        } catch (Exception e) {
            prioridade = 1; // Valor padrão em caso de erro
        }
        
        dto.setPrioridade(prioridade);
        dto.setCriadoPor((String) data.get("criadoPor"));
        
        if (data.get("status") != null) {
            dto.setStatus(TechDebt.StatusDebito.valueOf(data.get("status").toString()));
        }
        
        if (data.get("tipos") instanceof List) {
            List<String> tiposStr = (List<String>) data.get("tipos");
            dto.setTipos(tiposStr.stream().map(TechDebt.TipoDebito::valueOf).toList());
        }
        
        if (data.get("tags") instanceof List) {
            dto.setTags((List<String>) data.get("tags"));
        }
        
        return dto;
    }
    

    
    @PostMapping("/bulk-update-status")
    public ResponseEntity<?> bulkUpdateStatus(@RequestBody Map<String, Object> request,
                                             Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            List<Integer> idsInt = (List<Integer>) request.get("ids");
            List<Long> ids = idsInt.stream().map(Integer::longValue).toList();
            String status = (String) request.get("status");
            
            for (Long id : ids) {
                TechDebt debt = techDebtService.findById(id);
                if (debt != null) {
                    debt.setStatus(TechDebt.StatusDebito.fromString(status));
                    debt.setAlteradoPorId(user.getId());
                    techDebtService.save(debt);
                }
            }
            return ResponseEntity.ok().body("Status atualizado para " + ids.size() + " registros");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @PostMapping("/bulk-update-priority")
    public ResponseEntity<?> bulkUpdatePriority(@RequestBody Map<String, Object> request,
                                               Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            List<Integer> idsInt = (List<Integer>) request.get("ids");
            List<Long> ids = idsInt.stream().map(Integer::longValue).toList();
            Integer priority = (Integer) request.get("priority");
            
            for (Long id : ids) {
                TechDebt debt = techDebtService.findById(id);
                if (debt != null) {
                    debt.setPrioridade(priority);
                    debt.setAlteradoPorId(user.getId());
                    techDebtService.save(debt);
                }
            }
            return ResponseEntity.ok().body("Prioridade atualizada para " + ids.size() + " registros");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
    
    @PostMapping("/upload/sheet")
    public ResponseEntity<?> uploadSheet(@RequestParam("file") MultipartFile file,
                                        Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            List<TechDebt> debts = techDebtService.importFromCsv(file, user);
            return ResponseEntity.ok().body("Importados " + debts.size() + " débitos técnicos");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao importar planilha: " + e.getMessage());
        }
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            return userService.findOrCreateUser((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal());
        } else if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return userService.findByEmail(userDetails.getUsername());
        }
        throw new IllegalStateException("Tipo de autenticação não suportado");
    }
}