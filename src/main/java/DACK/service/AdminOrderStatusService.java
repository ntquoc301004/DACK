package DACK.service;

import DACK.model.Order;
import DACK.model.OrderDetail;
import DACK.model.OrderStatus;
import DACK.repo.BookRepository;
import DACK.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderStatusService {

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;

    public enum ChangeResult {
        OK,
        NOT_FOUND,
        ALREADY_CANCELLED,
        INVALID_TRANSITION
    }

    @Transactional
    public ChangeResult changeStatus(long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findAdminDetailById(orderId).orElse(null);
        if (order == null) {
            return ChangeResult.NOT_FOUND;
        }
        return apply(order, newStatus);
    }

    private ChangeResult apply(Order order, OrderStatus newStatus) {
        OrderStatus old = order.getStatus();
        if (old == OrderStatus.CANCELLED) {
            return ChangeResult.ALREADY_CANCELLED;
        }
        if (old == newStatus) {
            return ChangeResult.OK;
        }
        if (!isAllowedTransition(old, newStatus)) {
            return ChangeResult.INVALID_TRANSITION;
        }
        if (newStatus == OrderStatus.CANCELLED && stockWasDeducted(old)) {
            restock(order);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
        return ChangeResult.OK;
    }

    /** Đơn đang xử lý (chưa hủy) đều đã trừ tồn kho lúc đặt — hủy hoặc bỏ qua cần hoàn kho. */
    private static boolean stockWasDeducted(OrderStatus old) {
        return old == OrderStatus.PENDING || old == OrderStatus.PAID || old == OrderStatus.APPROVED;
    }

    private static boolean isAllowedTransition(OrderStatus old, OrderStatus neu) {
        if (old == OrderStatus.PENDING) {
            return neu == OrderStatus.PAID || neu == OrderStatus.CANCELLED;
        }
        if (old == OrderStatus.PAID) {
            return neu == OrderStatus.PENDING || neu == OrderStatus.APPROVED || neu == OrderStatus.CANCELLED;
        }
        if (old == OrderStatus.APPROVED) {
            return neu == OrderStatus.CANCELLED;
        }
        return false;
    }

    private void restock(Order order) {
        for (OrderDetail d : order.getDetails()) {
            var book = d.getBook();
            int current = book.getQuantity() == null ? 0 : book.getQuantity();
            book.setQuantity(current + d.getQuantity());
            bookRepository.save(book);
        }
    }

    /** Các trạng thái hiển thị trong dropdown (gồm trạng thái hiện tại). */
    public static List<OrderStatus> allowedTargets(OrderStatus current) {
        return switch (current) {
            case PENDING -> List.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.CANCELLED);
            case PAID -> List.of(OrderStatus.PAID, OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.CANCELLED);
            case APPROVED -> List.of(OrderStatus.APPROVED, OrderStatus.CANCELLED);
            case CANCELLED -> List.of(OrderStatus.CANCELLED);
        };
    }
}
