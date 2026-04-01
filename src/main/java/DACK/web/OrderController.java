package DACK.web;

import DACK.model.OrderStatuses;
import DACK.repo.OrderRepository;
import DACK.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderRepository orderRepository;
    private final CurrentUserService currentUserService;

    @GetMapping("/orders")
    public String myOrders(Model model) {
        var user = currentUserService.requireUser();
        model.addAttribute("orders", orderRepository.findByUserOrderByOrderDateDesc(user));
        model.addAttribute("username", user.getUsername());
        model.addAttribute("accountSection", "orders");
        model.addAttribute("accountOrderCount", orderRepository.countByUser(user));
        model.addAttribute("accountTotalPaid", orderRepository.sumTotalPriceByUserAndStatusIn(user, OrderStatuses.REVENUE_AND_SOLD));
        model.addAttribute("accountProfileIncomplete", isAccountIncomplete(user));
        return "orders/index";
    }

    private static boolean isAccountIncomplete(DACK.model.User user) {
        String p = user.getPhone();
        String prov = user.getProvince();
        String street = user.getStreetDetail();
        return p == null || p.isBlank()
                || prov == null || prov.isBlank()
                || street == null || street.isBlank();
    }
}

