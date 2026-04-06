package DACK.cart;

import DACK.model.User;
import DACK.model.UserCartItem;
import DACK.repo.BookRepository;
import DACK.repo.UserCartItemRepository;
import DACK.service.CurrentUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final BookRepository bookRepository;
    private final UserCartItemRepository userCartItemRepository;
    private final CurrentUserService currentUserService;

    public Collection<CartItem> items(HttpSession session) {
        User user = currentUserService.currentUserOrNull();
        if (user == null) {
            return List.of();
        }
        return userCartItemRepository.findByUserOrderByIdDesc(user).stream()
                .map(this::toCartItem)
                .toList();
    }

    public int countItems(HttpSession session) {
        return items(session).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal total(HttpSession session) {
        return items(session).stream()
                .map(CartItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void add(HttpSession session, Long bookId, int qty) {
        int addQty = Math.max(qty, 1);
        User user = currentUserService.requireUser();
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + bookId));

        if (book.getQuantity() == null || book.getQuantity() <= 0) {
            throw new IllegalStateException("Sách đã hết hàng");
        }

        UserCartItem existing = userCartItemRepository.findByUserIdAndBookId(user.getId(), bookId).orElse(null);
        if (existing != null) {
            int requested = existing.getQuantity() + addQty;
            if (requested > book.getQuantity()) {
                throw new IllegalStateException("Số lượng vượt quá tồn kho");
            }
            existing.setQuantity(requested);
            userCartItemRepository.save(existing);
            return;
        }
        if (addQty > book.getQuantity()) {
            throw new IllegalStateException("Số lượng vượt quá tồn kho");
        }
        UserCartItem item = new UserCartItem();
        item.setUser(user);
        item.setBook(book);
        item.setQuantity(addQty);
        userCartItemRepository.save(item);
    }

    @Transactional
    public void update(HttpSession session, Long bookId, int qty) {
        User user = currentUserService.requireUser();
        UserCartItem existing = userCartItemRepository.findByUserIdAndBookId(user.getId(), bookId).orElse(null);
        if (existing == null) {
            return;
        }
        if (qty <= 0) {
            userCartItemRepository.delete(existing);
            return;
        }
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + bookId));
        if (qty > book.getQuantity()) {
            throw new IllegalStateException("Số lượng vượt quá tồn kho");
        }
        existing.setQuantity(qty);
        userCartItemRepository.save(existing);
    }

    @Transactional
    public void remove(HttpSession session, Long bookId) {
        User user = currentUserService.currentUserOrNull();
        if (user == null) {
            return;
        }
        userCartItemRepository.findByUserIdAndBookId(user.getId(), bookId)
                .ifPresent(userCartItemRepository::delete);
    }

    @Transactional
    public void clear(HttpSession session) {
        User user = currentUserService.currentUserOrNull();
        if (user == null) {
            return;
        }
        userCartItemRepository.deleteByUser(user);
    }

    private CartItem toCartItem(UserCartItem item) {
        return new CartItem(
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getBook().getImage(),
                item.getBook().getPrice(),
                item.getQuantity()
        );
    }
}

