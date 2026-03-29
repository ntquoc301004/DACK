package DACK.cart;

import DACK.repo.BookRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final String SESSION_KEY = "CART";

    private final BookRepository bookRepository;

    @SuppressWarnings("unchecked")
    private Map<Long, CartItem> cartMap(HttpSession session) {
        Object existing = session.getAttribute(SESSION_KEY);
        if (existing instanceof Map<?, ?>) {
            return (Map<Long, CartItem>) existing;
        }
        Map<Long, CartItem> created = new LinkedHashMap<>();
        session.setAttribute(SESSION_KEY, created);
        return created;
    }

    public Collection<CartItem> items(HttpSession session) {
        return cartMap(session).values();
    }

    public int countItems(HttpSession session) {
        return cartMap(session).values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal total(HttpSession session) {
        return cartMap(session).values().stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void add(HttpSession session, Long bookId, int qty) {
        int addQty = Math.max(qty, 1);
        Map<Long, CartItem> cart = cartMap(session);
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + bookId));

        if (book.getQuantity() == null || book.getQuantity() <= 0) {
            throw new IllegalStateException("Sách đã hết hàng");
        }

        CartItem existing = cart.get(bookId);
        if (existing != null) {
            int requested = existing.getQuantity() + addQty;
            if (requested > book.getQuantity()) {
                throw new IllegalStateException("Số lượng vượt quá tồn kho");
            }
            existing.setQuantity(requested);
            return;
        }
        if (addQty > book.getQuantity()) {
            throw new IllegalStateException("Số lượng vượt quá tồn kho");
        }
        cart.put(bookId, new CartItem(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getImage(),
                book.getPrice(),
                addQty
        ));
    }

    public void update(HttpSession session, Long bookId, int qty) {
        Map<Long, CartItem> cart = cartMap(session);
        if (qty <= 0) {
            cart.remove(bookId);
            return;
        }
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + bookId));
        if (qty > book.getQuantity()) {
            throw new IllegalStateException("Số lượng vượt quá tồn kho");
        }
        CartItem existing = cart.get(bookId);
        if (existing != null) {
            existing.setQuantity(qty);
        }
    }

    public void remove(HttpSession session, Long bookId) {
        cartMap(session).remove(bookId);
    }

    public void clear(HttpSession session) {
        cartMap(session).clear();
    }
}

