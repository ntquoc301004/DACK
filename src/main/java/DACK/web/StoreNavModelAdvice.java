package DACK.web;

import DACK.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class StoreNavModelAdvice {

    private final CategoryRepository categoryRepository;

    @ModelAttribute("storeNavCategories")
    public Object storeNavCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
