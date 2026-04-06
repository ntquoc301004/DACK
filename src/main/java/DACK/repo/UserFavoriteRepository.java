package DACK.repo;

import DACK.model.User;
import DACK.model.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {
    List<UserFavorite> findByUserOrderByIdDesc(User user);

    Optional<UserFavorite> findByUserIdAndBookId(Long userId, Long bookId);
}