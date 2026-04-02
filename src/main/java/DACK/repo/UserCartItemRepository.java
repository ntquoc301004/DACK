package DACK.repo;

import DACK.model.User;
import DACK.model.UserCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCartItemRepository extends JpaRepository<UserCartItem, Long> {
    List<UserCartItem> findByUserOrderByIdDesc(User user);

    Optional<UserCartItem> findByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUser(User user);
}
