package DACK.service;

import DACK.model.Book;
import DACK.model.User;
import DACK.model.UserFavorite;
import DACK.repo.BookRepository;
import DACK.repo.UserFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final UserFavoriteRepository userFavoriteRepository;
    private final BookRepository bookRepository;

    public List<Book> favorites(User user) {
        return userFavoriteRepository.findByUserOrderByIdDesc(user).stream()
                .map(UserFavorite::getBook)
                .toList();
    }

    public boolean isFavorite(User user, Long bookId) {
        return userFavoriteRepository.findByUserIdAndBookId(user.getId(), bookId).isPresent();
    }

    public void addFavorite(User user, Long bookId) {
        if (userFavoriteRepository.findByUserIdAndBookId(user.getId(), bookId).isPresent()) {
            return;
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách: " + bookId));
        UserFavorite favorite = new UserFavorite();
        favorite.setUser(user);
        favorite.setBook(book);
        userFavoriteRepository.save(favorite);
    }

    public void removeFavorite(User user, Long bookId) {
        userFavoriteRepository.findByUserIdAndBookId(user.getId(), bookId)
                .ifPresent(userFavoriteRepository::delete);
    }
}
