package DACK.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminDashboardStats {
    private final BigDecimal revenueToday;
    private final BigDecimal revenueThisMonth;
    private final BigDecimal revenueAllTime;
    private final BigDecimal revenueLast7Days;
    private final BigDecimal revenuePrev7Days;
    private final long ordersToday;
    private final long ordersTotal;
    private final long ordersPending;
    private final long ordersPaid;
    private final long ordersApproved;
    private final long booksTotal;
    private final long customersTotal;
    private final long soldTodayQuantity;
    private final List<String> chartLabels;
    private final List<Double> chartRevenuePaid;
    private final List<Long> chartOrdersCount;
    /** 0–100, tỷ lệ doanh thu tháng / tổng doanh thu (minh họa) */
    private final int monthSharePercent;

    public Integer revenueWeekOverWeekPercent() {
        if (revenuePrev7Days == null || revenuePrev7Days.signum() == 0) {
            return revenueLast7Days != null && revenueLast7Days.signum() > 0 ? 100 : null;
        }
        return revenueLast7Days.subtract(revenuePrev7Days)
                .multiply(BigDecimal.valueOf(100))
                .divide(revenuePrev7Days, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
