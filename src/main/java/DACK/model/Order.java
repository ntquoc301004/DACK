package DACK.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "order_date", nullable = false, updatable = false)
    private Instant orderDate;

    @Column(name = "subtotal_price", precision = 12, scale = 2)
    private BigDecimal subtotalPrice = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "coupon_code", length = 64)
    private String couponCode;

    @Column(name = "coupon_description", length = 255)
    private String couponDescription;

    @NotNull
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    /** Snapshot địa chỉ giao hàng tại thời điểm đặt (đơn cũ có thể null). */
    @Column(name = "ship_recipient_name", length = 120)
    private String shipRecipientName;

    @Column(name = "ship_phone", length = 20)
    private String shipPhone;

    @Column(name = "ship_province", length = 80)
    private String shipProvince;

    @Column(name = "ship_district", length = 80)
    private String shipDistrict;

    @Column(name = "ship_ward", length = 80)
    private String shipWard;

    @Column(name = "ship_street_detail", length = 255)
    private String shipStreetDetail;

    @Column(name = "ship_note", length = 500)
    private String shipNote;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();
}

