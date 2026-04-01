package DACK.web;

import DACK.cart.CartService;
import DACK.repo.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class StoreNavModelAdvice {

    private final CategoryRepository categoryRepository;
    private final CartService cartService;

    @ModelAttribute("storeNavCategories")
    public Object storeNavCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount(HttpSession session) {
        return cartService.countItems(session);
    }
}
