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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tech-debts")
@RequiredArgsConstructor
public class TechDebtController {
    
    private final TechDebtService techDebtService;
    private final UserService userService;
    
    @GetMapping
    public String list(@AuthenticationPrincipal OAuth2User principal,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) Integer prioridade,
                      @RequestParam(required = false) String tipo,
                      Model model) {
        
        User user = userService.findOrCreateUser(principal);
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
        
        Page<TechDebt> debts = techDebtService.findByUserWithFilters(user, statusEnum, prioridade, tipoEnum, pageable);
        Map<String, Object> metrics = techDebtService.getDashboardMetrics(user);
        
        // Adicionar contadores específicos por status
        metrics.put("pause", techDebtService.countByUserAndStatus(user, TechDebt.StatusDebito.PAUSE));
        metrics.put("cancelled", techDebtService.countByUserAndStatus(user, TechDebt.StatusDebito.CANCELLED));
        metrics.put("test", techDebtService.countByUserAndStatus(user, TechDebt.StatusDebito.TEST));
        metrics.put("deploy", techDebtService.countByUserAndStatus(user, TechDebt.StatusDebito.DEPLOY));
        
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
    public String newForm(Model model) {
        model.addAttribute("techDebt", new TechDebt());
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Novo Débito Técnico");
        return "tech-debts/form";
    }
    
    @PostMapping
    public String save(@ModelAttribute TechDebt techDebt, 
                      @AuthenticationPrincipal OAuth2User principal) {
        User user = userService.findOrCreateUser(principal);
        techDebt.setUser(user);
        techDebt.setCriadoPor(user.getName());
        techDebtService.save(techDebt);
        return "redirect:/tech-debts";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        TechDebt techDebt = techDebtService.findById(id);
        model.addAttribute("techDebt", techDebt);
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Editar Débito Técnico");
        return "tech-debts/form";
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        techDebtService.delete(id);
        return "redirect:/tech-debts";
    }
    
    @PostMapping("/delete-multiple")
    @ResponseBody
    public ResponseEntity<?> deleteMultiple(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                techDebtService.delete(id);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir itens: " + e.getMessage());
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal, Model model) {
        User user = userService.findOrCreateUser(principal);
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
}