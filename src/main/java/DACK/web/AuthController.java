package DACK.web;

import DACK.model.RoleName;
import DACK.model.User;
import DACK.repo.RoleRepository;
import DACK.repo.UserRepository;
import DACK.web.dto.RegisterForm;
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
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegisterForm());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(
            @Valid @ModelAttribute("form") RegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (userRepository.existsByUsername(form.getUsername())) {
            bindingResult.rejectValue("username", "exists", "Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            bindingResult.rejectValue("email", "exists", "Email đã tồn tại");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/register";
        }

        var customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role missing"));

        User u = new User();
        u.setUsername(form.getUsername());
        u.setEmail(form.getEmail());
        u.setFullName(form.getFullName());
        u.setPassword(passwordEncoder.encode(form.getPassword()));
        u.getRoles().add(customerRole);
        userRepository.save(u);

        redirectAttributes.addFlashAttribute("flash", "Đăng ký thành công. Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}

