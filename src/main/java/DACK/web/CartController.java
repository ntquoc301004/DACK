package DACK.web;

import DACK.cart.CartService;
import DACK.discount.CouponService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        populateCartModel(session, model);
        return "cart/index";
    }

    @PostMapping("/cart/add/{bookId}")
    public Object add(
            @PathVariable Long bookId,
            @RequestParam(name = "qty", defaultValue = "1") int qty,
            @RequestParam(name = "redirect", defaultValue = "/home") String redirect,
            HttpSession session,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            RedirectAttributes redirectAttributes
    ) {
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(requestedWith);

        try {
            cartService.add(session, bookId, qty);

            if (ajax) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Da them vao gio hang",
                        "cartItemCount", cartService.countItems(session)
                ));
            }
            redirectAttributes.addFlashAttribute("flash", "Da them vao gio hang");

        } catch (IllegalStateException ex) {
            if (ajax) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "cartItemCount", cartService.countItems(session)
                ));
            }
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }

        return "redirect:" + redirect;
    }

    @PostMapping("/cart/update/{bookId}")
    public String update(
            @PathVariable Long bookId,
            @RequestParam("qty") int qty,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.update(session, bookId, qty);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/coupon")
    public String applyCoupon(
            @RequestParam("code") String code,
            @RequestParam(name = "redirect", defaultValue = "/cart") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        var result = couponService.applyCoupon(session, cartService.total(session), code);
        redirectAttributes.addFlashAttribute("flash", result.message());
        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/cart/coupon/remove")
    public String removeCoupon(
            @RequestParam(name = "redirect", defaultValue = "/cart") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        couponService.removeCoupon(session);
        redirectAttributes.addFlashAttribute("flash", "Da go ma giam gia.");
        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/cart/remove/{bookId}")
    public String remove(@PathVariable Long bookId, HttpSession session) {
        cartService.remove(session, bookId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clear(HttpSession session) {
        cartService.clear(session);
        couponService.clear(session);
        return "redirect:/cart";
    }

    private void populateCartModel(HttpSession session, Model model) {
        var items = cartService.items(session);
        var total = cartService.total(session);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("pricing", couponService.summarize(session, total));
    }

    private String safeRedirect(String redirect) {
        if ("/checkout".equals(redirect)) {
            return "/checkout";
        }
        return "/cart";
    }
}
