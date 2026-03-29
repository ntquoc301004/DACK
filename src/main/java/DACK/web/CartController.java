package DACK.web;

import DACK.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        model.addAttribute("items", cartService.items(session));
        model.addAttribute("total", cartService.total(session));
        return "cart/index";
    }

    @PostMapping("/cart/add/{bookId}")
    public String add(
            @PathVariable Long bookId,
            @RequestParam(name = "qty", defaultValue = "1") int qty,
            @RequestParam(name = "redirect", defaultValue = "/home") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.add(session, bookId, qty);
            redirectAttributes.addFlashAttribute("flash", "Đã thêm vào giỏ hàng");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:" + redirect;
    }

    @PostMapping("/cart/update/{bookId}")
    public String update(@PathVariable Long bookId, @RequestParam("qty") int qty, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.update(session, bookId, qty);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{bookId}")
    public String remove(@PathVariable Long bookId, HttpSession session) {
        cartService.remove(session, bookId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clear(HttpSession session) {
        cartService.clear(session);
        return "redirect:/cart";
    }
}

