package DACK.discount;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CouponSummary {
    private final BigDecimal subtotal;
    private final String couponCode;
    private final String couponDescription;
    private final BigDecimal discountAmount;
    private final BigDecimal total;

    public CouponSummary(
            BigDecimal subtotal,
            String couponCode,
            String couponDescription,
            BigDecimal discountAmount,
            BigDecimal total
    ) {
        this.subtotal = subtotal;
        this.couponCode = couponCode;
        this.couponDescription = couponDescription;
        this.discountAmount = discountAmount;
        this.total = total;
    }

    public boolean hasDiscount() {
        return couponCode != null && !couponCode.isBlank() && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
