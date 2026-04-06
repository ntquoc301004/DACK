package DACK.service;

import DACK.model.Order;
import DACK.web.dto.CheckoutAddressForm;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GiftAccessoryService {

    public static final BigDecimal GIFT_BOX_PRICE = BigDecimal.valueOf(25000);
    public static final BigDecimal GIFT_RIBBON_PRICE = BigDecimal.valueOf(12000);
    public static final BigDecimal GREETING_CARD_PRICE = BigDecimal.valueOf(15000);

    public BigDecimal calculateFee(CheckoutAddressForm form) {
        BigDecimal total = BigDecimal.ZERO;
        if (form.isGiftBox()) {
            total = total.add(GIFT_BOX_PRICE);
        }
        if (form.isGiftRibbon()) {
            total = total.add(GIFT_RIBBON_PRICE);
        }
        if (form.isGreetingCard()) {
            total = total.add(GREETING_CARD_PRICE);
        }
        return total;
    }

    public void applyToOrder(Order order, CheckoutAddressForm form) {
        order.setGiftBoxSelected(form.isGiftBox());
        order.setGiftRibbonSelected(form.isGiftRibbon());
        order.setGreetingCardSelected(form.isGreetingCard());
        order.setGiftMessage(normalize(form.getGiftMessage()));
        order.setGiftAccessoryFee(calculateFee(form));
    }

    public List<String> labelsFor(Order order) {
        List<String> labels = new ArrayList<>();
        if (order.isGiftBoxSelected()) {
            labels.add("Hop qua");
        }
        if (order.isGiftRibbonSelected()) {
            labels.add("No trang tri");
        }
        if (order.isGreetingCardSelected()) {
            labels.add("Thiep chuc mung");
        }
        return labels;
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        return value.isEmpty() ? null : value;
    }
}
