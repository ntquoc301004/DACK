package DACK.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartItem {
    private Long bookId;
    private String title;
    private String author;
    private String image;
    private BigDecimal unitPrice;
    private int quantity;
    private int availableStock;

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isOutOfStock() {
        return availableStock <= 0;
    }

    public boolean isQuantityExceeded() {
        return quantity > availableStock;
    }
}

