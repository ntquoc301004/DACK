package DACK.web;

import DACK.model.OrderStatuses;
import DACK.model.User;
import DACK.repo.OrderRepository;
import DACK.repo.UserRepository;
import DACK.service.CurrentUserService;
import DACK.web.dto.ChangePasswordForm;
import DACK.web.dto.ProfileForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/account/profile")
    public String profile(Model model) {
        User user = currentUserService.requireUser();
        addAccountAttrs(model, user);
        model.addAttribute("profileForm", profileFormFromUser(user));
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "account/profile";
    }

    @PostMapping("/account/profile")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser();
        String email = form.getEmail().trim();
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
            bindingResult.rejectValue("email", "email.duplicate", "Email này đã được tài khoản khác sử dụng");
        }
        if (bindingResult.hasErrors()) {
            addAccountAttrs(model, user);
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
            return "account/profile";
        }
        user.setEmail(email);
        user.setFullName(form.getFullName().trim());
        user.setPhone(emptyToNull(form.getPhone()));
        user.setProvince(emptyToNull(form.getProvince()));
        user.setDistrict(emptyToNull(form.getDistrict()));
        user.setWard(emptyToNull(form.getWard()));
        user.setStreetDetail(emptyToNull(form.getStreetDetail()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("flash", "Đã cập nhật thông tin tài khoản");
        return "redirect:/account/profile";
    }

    @PostMapping("/account/profile/password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser();
        if (bindingResult.hasErrors()) {
            addAccountAttrs(model, user);
            model.addAttribute("profileForm", profileFormFromUser(user));
            return "account/profile";
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            bindingResult.rejectValue("currentPassword", "currentPassword.invalid", "Mật khẩu hiện tại không đúng");
            addAccountAttrs(model, user);
            model.addAttribute("profileForm", profileFormFromUser(user));
            return "account/profile";
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("flash", "Đã đổi mật khẩu thành công");
        return "redirect:/account/profile";
    }

    private static ProfileForm profileFormFromUser(User user) {
        ProfileForm pf = new ProfileForm();
        pf.setEmail(user.getEmail());
        pf.setFullName(user.getFullName());
        pf.setPhone(blankToEmpty(user.getPhone()));
        pf.setProvince(blankToEmpty(user.getProvince()));
        pf.setDistrict(blankToEmpty(user.getDistrict()));
        pf.setWard(blankToEmpty(user.getWard()));
        pf.setStreetDetail(blankToEmpty(user.getStreetDetail()));
        return pf;
    }

    private void addAccountAttrs(Model model, User user) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("accountSection", "profile");
        model.addAttribute("accountOrderCount", orderRepository.countByUser(user));
        model.addAttribute("accountTotalPaid", orderRepository.sumTotalPriceByUserAndStatusIn(user, OrderStatuses.REVENUE_AND_SOLD));
        model.addAttribute("accountProfileIncomplete", isAccountIncomplete(user));
    }

    private static boolean isAccountIncomplete(User user) {
        String p = user.getPhone();
        String prov = user.getProvince();
        String street = user.getStreetDetail();
        return p == null || p.isBlank()
                || prov == null || prov.isBlank()
                || street == null || street.isBlank();
    }

    private static String blankToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String emptyToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
