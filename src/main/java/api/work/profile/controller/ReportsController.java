package api.work.profile.controller;

import api.work.profile.entity.User;
import api.work.profile.service.ProfileService;
import api.work.profile.service.TechDebtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ProfileService profileService;
    private final TechDebtService techDebtService;

    @GetMapping("/my-metrics")
    public String myMetrics(Authentication authentication, Model model) {
        User user = profileService.getUserFromAuthentication(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "reports/my-metrics";
    }

    @GetMapping("/tech-debts")
    public String techDebtsReport(Authentication authentication, Model model) {
        var user = profileService.getUserFromAuthentication(authentication);
        model.addAttribute("user", user);
        model.addAttribute("techDebts", techDebtService.findAllByUser(user));
        return "reports/tech-debts";
    }
    
    @GetMapping({"", "/"})
    public String reportsIndex() {
        return "redirect:/reports/my-metrics";
    }
    

}