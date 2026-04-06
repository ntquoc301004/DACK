package DACK.web;

import DACK.cart.CartService;
import DACK.model.Order;
import DACK.model.OrderDetail;
import DACK.model.OrderStatus;
import DACK.repo.BookRepository;
import DACK.repo.OrderRepository;
import DACK.service.CurrentUserService;
import DACK.service.GiftAccessoryService;
import DACK.web.dto.CheckoutAddressForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class CheckoutController {
    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final GiftAccessoryService giftAccessoryService;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        var user = currentUserService.requireUser();
        var form = new CheckoutAddressForm();
        form.setRecipientName(user.getFullName());
        form.setPhone(user.getPhone() != null ? user.getPhone() : "");
        form.setProvince(user.getProvince() != null ? user.getProvince() : "");
        form.setDistrict(user.getDistrict() != null ? user.getDistrict() : "");
        form.setWard(user.getWard() != null ? user.getWard() : "");
        form.setStreetDetail(user.getStreetDetail() != null ? user.getStreetDetail() : "");
        form.setNote("");
        form.setGiftMessage("");

        populateCheckoutModel(model, session, form);
        return "checkout/index";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @Valid @ModelAttribute("addressForm") CheckoutAddressForm addressForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        var items = cartService.items(session);
        if (items.isEmpty()) {
            redirectAttributes.addFlashAttribute("flash", "Gio hang dang trong");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            populateCheckoutModel(model, session, addressForm);
            return "checkout/index";
        }

        Order order = new Order();
        order.setUser(currentUserService.requireUser());
        order.setShipRecipientName(addressForm.getRecipientName().trim());
        order.setShipPhone(addressForm.getPhone().trim());
        order.setShipProvince(addressForm.getProvince().trim());
        order.setShipDistrict(addressForm.getDistrict().trim());
        order.setShipWard(addressForm.getWard().trim());
        order.setShipStreetDetail(addressForm.getStreetDetail().trim());
        order.setShipNote(addressForm.getNote() != null && !addressForm.getNote().isBlank()
                ? addressForm.getNote().trim() : null);
        giftAccessoryService.applyToOrder(order, addressForm);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (var item : items) {
            var book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new IllegalStateException("Khong tim thay sach: " + item.getBookId()));
            if (book.getQuantity() == null || book.getQuantity() < item.getQuantity()) {
                redirectAttributes.addFlashAttribute("flash", "So luong ton kho khong du cho sach: " + book.getTitle());
                return "redirect:/cart";
            }

            OrderDetail d = new OrderDetail();
            d.setOrder(order);
            d.setBook(book);
            d.setQuantity(item.getQuantity());
            d.setPrice(item.getUnitPrice());

            order.getDetails().add(d);
            total = total.add(item.lineTotal());

            book.setQuantity(book.getQuantity() - item.getQuantity());
            bookRepository.save(book);
        }

        total = total.add(order.getGiftAccessoryFee() == null ? BigDecimal.ZERO : order.getGiftAccessoryFee());
        order.setTotalPrice(total);

        orderRepository.save(order);
        cartService.clear(session);

        redirectAttributes.addFlashAttribute("flash", "Dat hang thanh cong");
        return "redirect:/orders";
    }

    private void populateCheckoutModel(Model model, HttpSession session, CheckoutAddressForm form) {
        BigDecimal subtotal = cartService.total(session);
        BigDecimal giftFee = giftAccessoryService.calculateFee(form);
        model.addAttribute("addressForm", form);
        model.addAttribute("items", cartService.items(session));
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("giftAccessoryFee", giftFee);
        model.addAttribute("giftBoxPrice", GiftAccessoryService.GIFT_BOX_PRICE);
        model.addAttribute("giftRibbonPrice", GiftAccessoryService.GIFT_RIBBON_PRICE);
        model.addAttribute("greetingCardPrice", GiftAccessoryService.GREETING_CARD_PRICE);
        model.addAttribute("total", subtotal.add(giftFee));
    }
}
