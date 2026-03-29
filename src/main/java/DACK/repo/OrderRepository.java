package DACK.repo;

import DACK.model.Order;
import DACK.model.OrderStatus;
import DACK.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.user = :user AND o.status = :status")
    BigDecimal sumTotalPriceByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.user = :user AND o.status IN :statuses")
    BigDecimal sumTotalPriceByUserAndStatusIn(@Param("user") User user, @Param("statuses") Collection<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.details d LEFT JOIN FETCH d.book WHERE o.id = :id")
    Optional<Order> findAdminDetailById(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalPriceByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumTotalPriceByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = :status AND o.orderDate >= :from AND o.orderDate < :to")
    BigDecimal sumTotalPriceByStatusAndOrderDateBetween(
            @Param("status") OrderStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status IN :statuses AND o.orderDate >= :from AND o.orderDate < :to")
    BigDecimal sumTotalPriceByStatusInAndOrderDateBetween(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    long countByStatus(OrderStatus status);

    long countByStatusIn(Collection<OrderStatus> statuses);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :from AND o.orderDate < :to")
    long countByOrderDateBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Order o JOIN o.details d WHERE o.status = :status AND o.orderDate >= :from AND o.orderDate < :to")
    Long sumDetailQuantityByStatusAndOrderDateBetween(
            @Param("status") OrderStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM Order o JOIN o.details d WHERE o.status IN :statuses AND o.orderDate >= :from AND o.orderDate < :to")
    Long sumDetailQuantityByStatusInAndOrderDateBetween(
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}

