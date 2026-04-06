package DACK.discount;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

@Service
public class CouponService {
    private static final String SESSION_KEY = "appliedCouponCode";

    private static final Map<String, CouponRule> COUPONS = Map.of(
            "SAVE10", CouponRule.percent("SAVE10", "Giam 10% toan bo don hang", new BigDecimal("0.10")),
            "LESS30K", CouponRule.fixed("LESS30K", "Giam truc tiep 30.000d", new BigDecimal("30000")),
            "BOOK50K", CouponRule.fixed("BOOK50K", "Giam truc tiep 50.000d", new BigDecimal("50000"))
    );

    public CouponSummary summarize(HttpSession session, BigDecimal subtotal) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new CouponSummary(BigDecimal.ZERO, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        String code = currentCode(session);
        if (code == null) {
            return new CouponSummary(safeSubtotal, null, null, BigDecimal.ZERO, safeSubtotal);
        }

        CouponRule rule = COUPONS.get(code);
        if (rule == null) {
            session.removeAttribute(SESSION_KEY);
            return new CouponSummary(safeSubtotal, null, null, BigDecimal.ZERO, safeSubtotal);
        }

        BigDecimal discountAmount = rule.discountFor(safeSubtotal);
        BigDecimal total = safeSubtotal.subtract(discountAmount).max(BigDecimal.ZERO);
        return new CouponSummary(safeSubtotal, rule.code(), rule.description(), discountAmount, total);
    }

    public ApplyResult applyCoupon(HttpSession session, BigDecimal subtotal, String rawCode) {
        BigDecimal safeSubtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        if (safeSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new ApplyResult(false, "Gio hang dang trong, chua the ap ma giam gia.");
        }

        String normalizedCode = normalize(rawCode);
        if (normalizedCode == null) {
            return new ApplyResult(false, "Vui long nhap ma giam gia.");
        }

        CouponRule rule = COUPONS.get(normalizedCode);
        if (rule == null) {
            return new ApplyResult(false, "Ma giam gia khong hop le.");
        }

        session.setAttribute(SESSION_KEY, normalizedCode);
        return new ApplyResult(true, "Da ap dung ma " + normalizedCode + ".");
    }

    public void removeCoupon(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
    }

    public void clear(HttpSession session) {
        removeCoupon(session);
    }

    private String currentCode(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        return value instanceof String code && !code.isBlank() ? code : null;
    }

    private String normalize(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    public record ApplyResult(boolean success, String message) {
    }

    private record CouponRule(
            String code,
            String description,
            BigDecimal fixedAmount,
            BigDecimal percentRate
    ) {
        static CouponRule fixed(String code, String description, BigDecimal amount) {
            return new CouponRule(code, description, amount, null);
        }

        static CouponRule percent(String code, String description, BigDecimal rate) {
            return new CouponRule(code, description, null, rate);
        }

        BigDecimal discountFor(BigDecimal subtotal) {
            BigDecimal discount = fixedAmount != null
                    ? fixedAmount
                    : subtotal.multiply(percentRate).setScale(2, RoundingMode.HALF_UP);
            return discount.min(subtotal);
        }
    }
}
