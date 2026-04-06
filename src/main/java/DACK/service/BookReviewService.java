package DACK.service;

import DACK.model.Book;
import DACK.model.BookReview;
import DACK.model.OrderStatuses;
import DACK.model.User;
import DACK.repo.BookReviewRepository;
import DACK.repo.OrderDetailRepository;
import DACK.web.dto.BookReviewForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookReviewService {

    private final BookReviewRepository bookReviewRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional(readOnly = true)
    public List<BookReview> reviewsForBook(long bookId) {
        return bookReviewRepository.findAllByBookIdForDisplay(bookId);
    }

    @Transactional(readOnly = true)
    public long reviewCount(long bookId) {
        return bookReviewRepository.countByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public double averageRating(long bookId) {
        Double avg = bookReviewRepository.averageRatingByBookId(bookId);
        return avg == null ? 0.0 : avg;
    }

    @Transactional(readOnly = true)
    public BookReview findExisting(long bookId, long userId) {
        return bookReviewRepository.findByBookIdAndUserId(bookId, userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean canReview(long bookId, long userId) {
        return orderDetailRepository.existsPurchasedBookByUser(bookId, userId, OrderStatuses.REVENUE_AND_SOLD);
    }

    @Transactional
    public void saveReview(Book book, User user, BookReviewForm form) {
        BookReview review = bookReviewRepository.findByBookIdAndUserId(book.getId(), user.getId())
                .orElseGet(BookReview::new);
        review.setBook(book);
        review.setUser(user);
        review.setRating(form.getRating());
        review.setComment(normalizeComment(form.getComment()));
        bookReviewRepository.save(review);
    }

    private static String normalizeComment(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        return value.isEmpty() ? null : value;
    }
}
