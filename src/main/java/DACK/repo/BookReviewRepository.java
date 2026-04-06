package DACK.repo;

import DACK.model.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {

    @Query("SELECT r FROM BookReview r JOIN FETCH r.user WHERE r.book.id = :bookId ORDER BY r.updatedAt DESC, r.id DESC")
    List<BookReview> findAllByBookIdForDisplay(@Param("bookId") Long bookId);

    Optional<BookReview> findByBookIdAndUserId(Long bookId, Long userId);

    long countByBookId(Long bookId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM BookReview r WHERE r.book.id = :bookId")
    Double averageRatingByBookId(@Param("bookId") Long bookId);
}
