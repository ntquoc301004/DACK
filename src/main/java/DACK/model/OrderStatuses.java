package DACK.model;

import java.util.List;

public final class OrderStatuses {

    private OrderStatuses() {}

    /**
     * Đơn được tính doanh thu và số lượng đã bán (đã thanh toán hoặc admin đã duyệt giao).
     */
    public static final List<OrderStatus> REVENUE_AND_SOLD = List.of(OrderStatus.PAID, OrderStatus.APPROVED);

    public static boolean countsTowardRevenue(OrderStatus s) {
        return s == OrderStatus.PAID || s == OrderStatus.APPROVED;
    }
}
