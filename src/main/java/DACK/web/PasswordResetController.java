package DACK.web;

import DACK.service.PasswordResetMailService;
import DACK.service.PasswordResetService;
import DACK.web.dto.ForgotPasswordForm;
import DACK.web.dto.ResetPasswordForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private static final String GENERIC_REQUEST_MESSAGE =
            "Nếu email đã được đăng ký, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.";

    private final PasswordResetService passwordResetService;
    private final PasswordResetMailService passwordResetMailService;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ForgotPasswordForm());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @Valid @ModelAttribute("form") ForgotPasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        var issued = passwordResetService.issueResetToken(form.getEmail());
        if (issued.isEmpty()) {
            redirectAttributes.addFlashAttribute("flash", GENERIC_REQUEST_MESSAGE);
            return "redirect:/login";
        }

        String resetUrl = buildResetUrl(issued.get().token());
        if (!passwordResetMailService.sendResetLink(issued.get().recipientEmail(), resetUrl)) {
            redirectAttributes.addFlashAttribute("flash",
                    "Không gửi được email. Kiểm tra cấu hình SMTP hoặc thử lại sau.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("flash", GENERIC_REQUEST_MESSAGE);
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(
            @RequestParam(value = "token", required = false) String token,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || token.isBlank() || !passwordResetService.isTokenValid(token)) {
            redirectAttributes.addFlashAttribute("flash", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        if (!model.containsAttribute("form")) {
            ResetPasswordForm form = new ResetPasswordForm();
            model.addAttribute("form", form);
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(
            @RequestParam("token") String token,
            @Valid @ModelAttribute("form") ResetPasswordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (token == null || token.isBlank() || !passwordResetService.isTokenValid(token)) {
            redirectAttributes.addFlashAttribute("flash", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "auth/reset-password";
        }

        if (!passwordResetService.resetPassword(token, form.getNewPassword())) {
            redirectAttributes.addFlashAttribute("flash", "Liên kết không hợp lệ hoặc đã hết hạn.");
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute("flash", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập.");
        return "redirect:/login";
    }

    private String buildResetUrl(String token) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.trim().replaceAll("/$", "");
            return base + "/reset-password?token=" + token;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reset-password")
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();
    }
}
