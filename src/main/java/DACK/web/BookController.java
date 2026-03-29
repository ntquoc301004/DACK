package DACK.web;

import DACK.model.OrderStatuses;
import DACK.repo.BookRepository;
import DACK.repo.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookRepository bookRepository;
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));
        model.addAttribute("book", book);
        model.addAttribute("soldCount", orderDetailRepository.sumQuantityByBookIdAndOrderStatusIn(id, OrderStatuses.REVENUE_AND_SOLD));
        return "books/detail";
    }
}

