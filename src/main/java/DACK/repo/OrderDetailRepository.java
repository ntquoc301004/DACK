package DACK.repo;

import DACK.model.OrderDetail;
import DACK.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM OrderDetail d JOIN d.order o WHERE d.book.id = :bookId AND o.status = :status")
    long sumQuantityByBookIdAndOrderStatus(@Param("bookId") Long bookId, @Param("status") OrderStatus status);

    @Query("SELECT d.book.id, COALESCE(SUM(d.quantity), 0) FROM OrderDetail d JOIN d.order o WHERE o.status = :status AND d.book.id IN :ids GROUP BY d.book.id")
    List<Object[]> sumSoldQuantityGroupedByBookId(@Param("ids") List<Long> ids, @Param("status") OrderStatus status);

    @Query("SELECT COALESCE(SUM(d.quantity), 0) FROM OrderDetail d JOIN d.order o WHERE d.book.id = :bookId AND o.status IN :statuses")
    long sumQuantityByBookIdAndOrderStatusIn(@Param("bookId") Long bookId, @Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT d.book.id, COALESCE(SUM(d.quantity), 0) FROM OrderDetail d JOIN d.order o WHERE o.status IN :statuses AND d.book.id IN :ids GROUP BY d.book.id")
    List<Object[]> sumSoldQuantityGroupedByBookIdIn(@Param("ids") List<Long> ids, @Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
            SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END
            FROM OrderDetail d
            JOIN d.order o
            WHERE d.book.id = :bookId
              AND o.user.id = :userId
              AND o.status IN :statuses
            """)
    boolean existsPurchasedBookByUser(
            @Param("bookId") Long bookId,
            @Param("userId") Long userId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}
