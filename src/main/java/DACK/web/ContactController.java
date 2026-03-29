package DACK.web;

import DACK.service.ContactMailService;
import DACK.web.dto.ContactFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final ContactMailService contactMailService;

    @GetMapping("/contact")
    public String contactForm(Model model) {
        if (!model.containsAttribute("contactForm")) {
            model.addAttribute("contactForm", new ContactFormDto());
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(
            @Valid @ModelAttribute("contactForm") ContactFormDto form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "contact";
        }
        if (contactMailService.sendContactMessage(form)) {
            redirectAttributes.addFlashAttribute("flash", "Cảm ơn bạn! Tin nhắn đã được gửi tới hộp thư quản trị. Chúng tôi sẽ phản hồi sớm.");
            return "redirect:/contact";
        }
        redirectAttributes.addFlashAttribute("flash",
                "Hiện chưa gửi được email. Vui lòng cấu hình SMTP (spring.mail.*) và contact.inbox trong application.properties, hoặc liên hệ hotline / email hiển thị bên trái.");
        return "contact";
    }
}
