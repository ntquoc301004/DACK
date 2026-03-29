package DACK.service;

import DACK.model.OrderStatus;
import DACK.model.OrderStatuses;
import DACK.model.RoleName;
import DACK.repo.BookRepository;
import DACK.repo.OrderRepository;
import DACK.repo.UserRepository;
import DACK.web.dto.AdminDashboardStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public AdminDashboardStats buildStats() {
        ZonedDateTime now = ZonedDateTime.now(VN);
        LocalDate today = now.toLocalDate();

        Instant dayStart = today.atStartOfDay(VN).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(VN).toInstant();

        LocalDate firstOfMonth = today.withDayOfMonth(1);
        Instant monthStart = firstOfMonth.atStartOfDay(VN).toInstant();
        Instant monthEnd = today.plusDays(1).atStartOfDay(VN).toInstant();

        BigDecimal revenueToday = orderRepository.sumTotalPriceByStatusInAndOrderDateBetween(
                OrderStatuses.REVENUE_AND_SOLD, dayStart, dayEnd);
        BigDecimal revenueThisMonth = orderRepository.sumTotalPriceByStatusInAndOrderDateBetween(
                OrderStatuses.REVENUE_AND_SOLD, monthStart, monthEnd);
        BigDecimal revenueAllTime = orderRepository.sumTotalPriceByStatusIn(OrderStatuses.REVENUE_AND_SOLD);
        int monthShare = 0;
        if (revenueAllTime != null && revenueAllTime.signum() > 0 && revenueThisMonth != null) {
            monthShare = revenueThisMonth.multiply(BigDecimal.valueOf(100))
                    .divide(revenueAllTime, 0, RoundingMode.HALF_UP)
                    .min(BigDecimal.valueOf(100))
                    .intValue();
        }

        Instant last7Start = now.minusDays(6).toLocalDate().atStartOfDay(VN).toInstant();
        Instant last7End = today.plusDays(1).atStartOfDay(VN).toInstant();
        Instant prev7Start = now.minusDays(13).toLocalDate().atStartOfDay(VN).toInstant();
        Instant prev7End = last7Start;

        BigDecimal revenueLast7 = nz(orderRepository.sumTotalPriceByStatusInAndOrderDateBetween(
                OrderStatuses.REVENUE_AND_SOLD, last7Start, last7End));
        BigDecimal revenuePrev7 = nz(orderRepository.sumTotalPriceByStatusInAndOrderDateBetween(
                OrderStatuses.REVENUE_AND_SOLD, prev7Start, prev7End));

        long ordersToday = orderRepository.countByOrderDateBetween(dayStart, dayEnd);
        long ordersPending = orderRepository.countByStatus(OrderStatus.PENDING);
        long ordersPaid = orderRepository.countByStatus(OrderStatus.PAID);
        long ordersApproved = orderRepository.countByStatus(OrderStatus.APPROVED);
        long ordersTotal = orderRepository.count();

        long soldToday = java.util.Optional.ofNullable(
                orderRepository.sumDetailQuantityByStatusInAndOrderDateBetween(
                        OrderStatuses.REVENUE_AND_SOLD, dayStart, dayEnd)).orElse(0L);

        List<String> labels = new ArrayList<>();
        List<Double> revSeries = new ArrayList<>();
        List<Long> ordSeries = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            labels.add(d.format(fmt));
            Instant from = d.atStartOfDay(VN).toInstant();
            Instant to = d.plusDays(1).atStartOfDay(VN).toInstant();
            BigDecimal dayRev = orderRepository.sumTotalPriceByStatusInAndOrderDateBetween(
                    OrderStatuses.REVENUE_AND_SOLD, from, to);
            revSeries.add(dayRev != null ? dayRev.doubleValue() : 0.0);
            ordSeries.add(orderRepository.countByOrderDateBetween(from, to));
        }

        return AdminDashboardStats.builder()
                .revenueToday(nz(revenueToday))
                .revenueThisMonth(nz(revenueThisMonth))
                .revenueAllTime(nz(revenueAllTime))
                .revenueLast7Days(nz(revenueLast7))
                .revenuePrev7Days(nz(revenuePrev7))
                .ordersToday(ordersToday)
                .ordersTotal(ordersTotal)
                .ordersPending(ordersPending)
                .ordersPaid(ordersPaid)
                .ordersApproved(ordersApproved)
                .booksTotal(bookRepository.count())
                .customersTotal(userRepository.countUsersWithRole(RoleName.CUSTOMER))
                .soldTodayQuantity(soldToday)
                .chartLabels(labels)
                .chartRevenuePaid(revSeries)
                .chartOrdersCount(ordSeries)
                .monthSharePercent(monthShare)
                .build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
