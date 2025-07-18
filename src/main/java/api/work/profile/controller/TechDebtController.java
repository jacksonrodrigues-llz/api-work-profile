package api.work.profile.controller;

import api.work.profile.entity.TechDebt;
import api.work.profile.enums.UserRole;
import api.work.profile.service.ProfileService;
import api.work.profile.service.TechDebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tech-debts")
@RequiredArgsConstructor
public class TechDebtController {
    
    private final TechDebtService techDebtService;
    private final ProfileService profileService;
    
    @GetMapping
    public String list(Authentication authentication,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) Integer prioridade,
                      @RequestParam(required = false) String tipo,
                      jakarta.servlet.http.HttpServletRequest request,
                      Model model) {
        
        var user = profileService.getUserFromAuthentication(authentication);
        var pageable = PageRequest.of(page, size);
        
        var statusEnum = parseStatus(status);
        var tipoEnum = parseTipo(tipo);
        var search = request.getParameter("search");
        var taskNumber = request.getParameter("taskNumber");
        var periodo = request.getParameter("periodo");
        
        var debts = techDebtService.findAllWithFilters(statusEnum, prioridade, tipoEnum, search, taskNumber, periodo, pageable);
        var metrics = techDebtService.getAllDashboardMetrics();
        
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
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("techDebt", TechDebt.builder().build());
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Novo Débito Técnico");
        model.addAttribute("user", user);
        return "tech-debts/form";
    }
    
    @PostMapping
    public String save(@ModelAttribute TechDebt techDebt, Authentication authentication) {
        var user = profileService.getUserFromAuthentication(authentication);
        var toSave = techDebt.toBuilder()
            .user(user)
            .build();
        
        if (techDebt.getId() == null) {
            toSave = toSave.toBuilder()
                .criadoPor(user.getName())
                .criadoPorId(user.getId())
                .build();
        } else {
            toSave = toSave.toBuilder()
                .alteradoPorId(user.getId())
                .build();
        }
        
        techDebtService.save(toSave);
        return "redirect:/tech-debts";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var techDebt = techDebtService.findById(id);
        model.addAttribute("techDebt", techDebt);
        model.addAttribute("statusOptions", TechDebt.StatusDebito.values());
        model.addAttribute("tipoOptions", TechDebt.TipoDebito.values());
        model.addAttribute("title", "Editar Débito Técnico");
        model.addAttribute("user", user);
        return "tech-debts/form";
    }
    
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        var user = profileService.getUserFromAuthentication(authentication);
        if (user.getRole() != UserRole.ADMIN) {
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
            var user = profileService.getUserFromAuthentication(authentication);
            if (user.getRole() != UserRole.ADMIN) {
                return ResponseEntity.badRequest().body("Apenas administradores podem excluir débitos técnicos");
            }
            ids.forEach(techDebtService::delete);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir itens: " + e.getMessage());
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        var metrics = techDebtService.getDashboardMetrics(user);
        var pageable = PageRequest.of(0, 10);
        var criticalDebts = techDebtService.findByUserWithFilters(user, null, 1, null, pageable);
        
        model.addAttribute("metrics", metrics);
        model.addAttribute("criticalDebts", criticalDebts.getContent());
        model.addAttribute("title", "Dashboard - Débitos Técnicos");
        
        return "tech-debts/dashboard";
    }
    
    @GetMapping("/search")
    @ResponseBody
    public List<api.work.profile.dto.TechDebtSearchDTO> searchByText(@RequestParam String q) {
        return techDebtService.searchByText(q);
    }
    
    @GetMapping("/search-task")
    @ResponseBody
    public List<api.work.profile.dto.TechDebtSearchDTO> searchByTaskNumber(@RequestParam String taskNumber) {
        return techDebtService.searchByTaskNumber(taskNumber);
    }
    
    private TechDebt.StatusDebito parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return TechDebt.StatusDebito.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private TechDebt.TipoDebito parseTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) return null;
        try {
            return TechDebt.TipoDebito.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}