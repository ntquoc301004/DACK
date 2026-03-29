package DACK.repo;

import DACK.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    long countByCategoryId(Long categoryId);
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCaseAndCategoryId(String title, Long categoryId, Pageable pageable);
    List<Book> findTop3ByCategoryIdOrderByIdDesc(Long categoryId);

    List<Book> findTop3ByOrderByIdDesc();

    Optional<Book> findFirstByOrderByIdDesc();
}

