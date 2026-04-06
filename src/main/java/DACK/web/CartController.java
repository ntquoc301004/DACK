package DACK.web;

import DACK.cart.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequiredArgsConstructor // Tự động tạo Constructor cho các field 'final' (Dependency Injection)
public class CartController {
    
    private final CartService cartService;

    /**
     * Hiển thị trang giỏ hàng
     */
    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        var items = cartService.items(session);
        model.addAttribute("items", items);
        model.addAttribute("total", cartService.total(session));
        model.addAttribute("cartHasIssues", cartService.hasStockIssues(session));
        return "cart/index"; // Trả về file HTML tại src/main/resources/templates/cart/index.html
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Hỗ trợ cả Request thông thường và Ajax
     */
    @PostMapping("/cart/add/{bookId}")
    public Object add(
            @PathVariable Long bookId, // ID của sách lấy từ URL
            @RequestParam(name = "qty", defaultValue = "1") int qty, // Số lượng, mặc định là 1
            @RequestParam(name = "redirect", defaultValue = "/home") String redirect, // Trang cần quay lại sau khi thêm
            HttpSession session,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith, // Kiểm tra xem có phải gọi bằng Ajax không
            RedirectAttributes redirectAttributes // Dùng để gửi thông báo (flash message) khi chuyển trang
    ) {
        // Kiểm tra nếu request là Ajax (XMLHttpRequest)
        boolean ajax = "XMLHttpRequest".equalsIgnoreCase(requestedWith);
        
        try {
            cartService.add(session, bookId, qty); // Gọi logic thêm vào giỏ hàng
            
            if (ajax) {
                // Nếu là Ajax: Trả về JSON để Frontend xử lý mà không cần load lại trang
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã thêm vào giỏ hàng",
                        "cartItemCount", cartService.countItems(session)
                ));
            }
            // Nếu là Request thông thường: Hiển thị thông báo tạm thời
            redirectAttributes.addFlashAttribute("flash", "Đã thêm vào giỏ hàng");
            
        } catch (IllegalStateException ex) {
            // Xử lý lỗi (ví dụ: hết hàng, số lượng không hợp lệ)
            if (ajax) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "cartItemCount", cartService.countItems(session)
                ));
            }
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }
        
        // Quay lại trang trước đó hoặc trang mặc định
        return "redirect:" + redirect;
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @PostMapping("/cart/update/{bookId}")
    public String update(@PathVariable Long bookId, @RequestParam("qty") int qty, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.update(session, bookId, qty);
        } catch (IllegalStateException ex) {
            // Lưu thông báo lỗi nếu cập nhật thất bại (ví dụ: vượt quá kho)
            redirectAttributes.addFlashAttribute("flash", ex.getMessage());
        }
        return "redirect:/cart"; // Cập nhật xong quay lại trang giỏ hàng
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     */
    @PostMapping("/cart/remove/{bookId}")
    public String remove(@PathVariable Long bookId, HttpSession session) {
        cartService.remove(session, bookId);
        return "redirect:/cart";
    }

    /**
     * Xóa sạch toàn bộ giỏ hàng
     */
    @PostMapping("/cart/clear")
    public String clear(HttpSession session) {
        cartService.clear(session);
        return "redirect:/cart";
    }
}