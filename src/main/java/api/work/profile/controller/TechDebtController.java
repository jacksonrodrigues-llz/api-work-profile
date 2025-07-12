package api.work.profile.controller;

import api.work.profile.dto.TechDebtDTO;
import api.work.profile.entity.TechDebt;
import api.work.profile.entity.User;
import api.work.profile.service.TechDebtService;
import api.work.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tech-debts")
@RequiredArgsConstructor
public class TechDebtController {
    
    private final TechDebtService techDebtService;
    private final UserService userService;
    
    @GetMapping
    public String list(Authentication authentication,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) Integer prioridade,
                      @RequestParam(required = false) String tipo,
                      Model model) {
        
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        TechDebt.StatusDebito statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = TechDebt.StatusDebito.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignorar valores inválidos
            }
        }
        
        TechDebt.TipoDebito tipoEnum = null;
        if (tipo != null && !tipo.trim().isEmpty()) {
            try {
                tipoEnum = TechDebt.TipoDebito.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignorar valores inválidos
            }
        }
        
        Page<TechDebt> debts = techDebtService.findAllWithFilters(statusEnum, prioridade, tipoEnum, pageable);
        Map<String, Object> metrics = techDebtService.getAllDashboardMetrics();
        
        // Adicionar contadores específicos por status
        metrics.put("pause", techDebtService.countByStatus(TechDebt.StatusDebito.PAUSE));
        metrics.put("cancelled", techDebtService.countByStatus(TechDebt.StatusDebito.CANCELLED));
        metrics.put("test", techDebtService.countByStatus(TechDebt.StatusDebito.TEST));
        metrics.put("deploy", techDebtService.countByStatus(TechDebt.StatusDebito.DEPLOY));
        
        model.addAttribute("debts", debts);
        model.addAttribute("metrics", metrics);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", debts.getTotalPages());
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Débitos Técnicos");
        model.addAttribute("user", user);
        
        return "tech-debts/list";
    }
    
    @GetMapping("/new")
    public String newForm(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        model.addAttribute("techDebt", new TechDebt());
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Novo Débito Técnico");
        model.addAttribute("user", user);
        return "tech-debts/form";
    }
    
    @PostMapping
    public String save(@ModelAttribute TechDebt techDebt, 
                      Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        techDebt.setUser(user);
        techDebt.setCriadoPor(user.getName());
        techDebt.setCriadoPorId(user.getId());
        if (techDebt.getId() != null) {
            techDebt.setAlteradoPorId(user.getId());
        }
        techDebtService.save(techDebt);
        return "redirect:/tech-debts";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        TechDebt techDebt = techDebtService.findById(id);
        model.addAttribute("techDebt", techDebt);
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Editar Débito Técnico");
        model.addAttribute("user", user);
        return "tech-debts/form";
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuthentication(authentication);
        if (user.getRole() != api.work.profile.enums.UserRole.ADMIN) {
            redirectAttributes.addFlashAttribute("errorMessage", "Apenas administradores podem excluir débitos técnicos");
            return "redirect:/tech-debts";
        }
        techDebtService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Débito técnico excluído com sucesso!");
        return "redirect:/tech-debts";
    }
    
    @PostMapping("/delete-multiple")
    @ResponseBody
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids, Authentication authentication) {
        try {
            User user = getUserFromAuthentication(authentication);
            if (user.getRole() != api.work.profile.enums.UserRole.ADMIN) {
                return ResponseEntity.badRequest().body("Apenas administradores podem excluir débitos técnicos");
            }
            for (Long id : ids) {
                techDebtService.delete(id);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir itens: " + e.getMessage());
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = getUserFromAuthentication(authentication);
        Map<String, Object> metrics = techDebtService.getDashboardMetrics(user);
        
        // Buscar débitos críticos (prioridade 1)
        Pageable pageable = PageRequest.of(0, 10);
        Page<TechDebt> criticalDebts = techDebtService.findByUserWithFilters(
            user, null, 1, null, pageable);
        
        model.addAttribute("metrics", metrics);
        model.addAttribute("criticalDebts", criticalDebts.getContent());
        model.addAttribute("title", "Dashboard - Débitos Técnicos");
        
        return "tech-debts/dashboard";
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