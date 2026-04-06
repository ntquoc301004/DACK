package DACK.web;

import DACK.model.OrderStatuses;
import DACK.model.User;
import DACK.repo.OrderRepository;
import DACK.service.CurrentUserService;
import DACK.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WishlistController {
    private final CurrentUserService currentUserService;
    private final FavoriteService favoriteService;
    private final OrderRepository orderRepository;

    @GetMapping("/account/wishlist")
    public String wishlist(Model model) {
        User user = currentUserService.requireUser();
        addAccountAttrs(model, user);
        model.addAttribute("favorites", favoriteService.favorites(user));
        model.addAttribute("accountSection", "wishlist");
        return "account/wishlist";
    }

    @PostMapping("/account/wishlist/add/{bookId}")
    public String addFavorite(
            @PathVariable Long bookId,
            @RequestParam(name = "redirect", required = false) String redirect,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser();
        favoriteService.addFavorite(user, bookId);
        redirectAttributes.addFlashAttribute("flash", "Đã thêm vào danh sách yêu thích");
        return "redirect:" + chooseRedirect(redirect, referer);
    }

    @PostMapping("/account/wishlist/remove/{bookId}")
    public String removeFavorite(
            @PathVariable Long bookId,
            @RequestParam(name = "redirect", required = false) String redirect,
            @RequestHeader(value = "Referer", required = false) String referer,
            RedirectAttributes redirectAttributes
    ) {
        User user = currentUserService.requireUser();
        favoriteService.removeFavorite(user, bookId);
        redirectAttributes.addFlashAttribute("flash", "Đã xóa khỏi danh sách yêu thích");
        return "redirect:" + chooseRedirect(redirect, referer);
    }

    private String chooseRedirect(String redirect, String referer) {
        if (redirect != null && !redirect.isBlank()) {
            return redirect;
        }
        return referer != null ? referer : "/account/wishlist";
    }

    private void addAccountAttrs(Model model, User user) {
        model.addAttribute("username", user.getUsername());
        model.addAttribute("accountSection", "wishlist");
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
}
