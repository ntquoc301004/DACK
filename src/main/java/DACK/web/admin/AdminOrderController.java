package DACK.web.admin;

import DACK.model.Order;
import DACK.model.OrderStatus;
import DACK.repo.OrderRepository;
import DACK.service.AdminOrderStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final AdminOrderStatusService adminOrderStatusService;

    private void addAdminLayoutAttrs(Model model, String pageTitle) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("adminDisplayName", auth != null ? auth.getName() : "Admin");
        model.addAttribute("adminPageTitle", pageTitle);
        model.addAttribute("adminNavActive", "orders");
    }

    @GetMapping
    public String list(
            @RequestParam(name = "status", required = false) String statusParam,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {
        int safePage = Math.max(page, 0);
        var pageable = PageRequest.of(safePage, 15, Sort.by(Sort.Direction.DESC, "orderDate"));
        OrderStatus filter = parseStatus(statusParam);
        var orders = filter == null
                ? orderRepository.findAllByOrderByOrderDateDesc(pageable)
                : orderRepository.findByStatusOrderByOrderDateDesc(filter, pageable);
        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", filter);
        model.addAttribute("orderStatuses", OrderStatus.values());
        addAdminLayoutAttrs(model, "Quản lý đơn hàng");
        return "admin/orders/index";
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderRepository.findAdminDetailById(id).orElseThrow();
        model.addAttribute("order", order);
        model.addAttribute("allowedStatusChanges", AdminOrderStatusService.allowedTargets(order.getStatus()));
        addAdminLayoutAttrs(model, "Chi tiết đơn #" + id);
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String statusRaw,
            RedirectAttributes redirectAttributes
    ) {
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(statusRaw);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("flash", "Trạng thái không hợp lệ");
            return "redirect:/admin/orders/" + id;
        }
        return flashAfterChange(id, adminOrderStatusService.changeStatus(id, target), redirectAttributes);
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return flashAfterChange(id, adminOrderStatusService.changeStatus(id, OrderStatus.APPROVED), redirectAttributes);
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return flashAfterChange(id, adminOrderStatusService.changeStatus(id, OrderStatus.CANCELLED), redirectAttributes);
    }

    private static String flashAfterChange(long id, AdminOrderStatusService.ChangeResult result, RedirectAttributes ra) {
        switch (result) {
            case OK -> ra.addFlashAttribute("flash", "Đã cập nhật trạng thái đơn hàng");
            case NOT_FOUND -> ra.addFlashAttribute("flash", "Không tìm thấy đơn hàng");
            case ALREADY_CANCELLED -> ra.addFlashAttribute("flash", "Đơn đã hủy trước đó, không đổi được trạng thái");
            case INVALID_TRANSITION -> ra.addFlashAttribute("flash", "Không thể chuyển sang trạng thái này");
        }
        return "redirect:/admin/orders/" + id;
    }

    private static OrderStatus parseStatus(String param) {
        if (param == null || param.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(param.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
