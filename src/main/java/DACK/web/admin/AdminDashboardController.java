package DACK.web.admin;

import DACK.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("adminDisplayName", auth != null ? auth.getName() : "Admin");
        model.addAttribute("adminPageTitle", "Bảng điều khiển");
        model.addAttribute("adminNavActive", "dashboard");
        model.addAttribute("stats", adminDashboardService.buildStats());
        return "admin/dashboard";
    }
}
