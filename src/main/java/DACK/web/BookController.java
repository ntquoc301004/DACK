package DACK.web;

import DACK.model.Book;
import DACK.model.OrderStatuses;
import DACK.repo.BookRepository;
import DACK.repo.OrderDetailRepository;
import DACK.service.BookReviewService;
import DACK.service.CurrentUserService;
import DACK.web.dto.BookReviewForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookRepository bookRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CurrentUserService currentUserService;
    private final BookReviewService bookReviewService;

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Book book = findBook(id);
        populateDetailModel(model, book, buildInitialReviewForm(book));
        return "books/detail";
    }

    @PostMapping("/books/{id}/reviews")
    public String submitReview(
            @PathVariable Long id,
            @Valid @ModelAttribute("reviewForm") BookReviewForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Book book = findBook(id);
        var user = currentUserService.requireUser();
        if (!bookReviewService.canReview(book.getId(), user.getId())) {
            redirectAttributes.addFlashAttribute("flash", "Ban can mua sach nay truoc khi danh gia");
            return "redirect:/books/" + id;
        }
        if (bindingResult.hasErrors()) {
            populateDetailModel(model, book, form);
            return "books/detail";
        }

        bookReviewService.saveReview(book, user, form);
        redirectAttributes.addFlashAttribute("flash", "Da gui danh gia cua ban");
        return "redirect:/books/" + id;
    }

    private void populateDetailModel(Model model, Book book, BookReviewForm reviewForm) {
        model.addAttribute("book", book);
        model.addAttribute("soldCount", orderDetailRepository.sumQuantityByBookIdAndOrderStatusIn(book.getId(), OrderStatuses.REVENUE_AND_SOLD));
        model.addAttribute("reviews", bookReviewService.reviewsForBook(book.getId()));
        model.addAttribute("reviewCount", bookReviewService.reviewCount(book.getId()));
        model.addAttribute("averageRating", bookReviewService.averageRating(book.getId()));
        model.addAttribute("reviewForm", reviewForm);

        var user = currentUserService.currentUserOrNull();
        boolean canReview = user != null && bookReviewService.canReview(book.getId(), user.getId());
        model.addAttribute("canReview", canReview);
        model.addAttribute("existingReview", user == null ? null : bookReviewService.findExisting(book.getId(), user.getId()));
    }

    private BookReviewForm buildInitialReviewForm(Book book) {
        BookReviewForm form = new BookReviewForm();
        var user = currentUserService.currentUserOrNull();
        if (user == null) {
            return form;
        }
        var existing = bookReviewService.findExisting(book.getId(), user.getId());
        if (existing != null) {
            form.setRating(existing.getRating());
            form.setComment(existing.getComment());
        }
        return form;
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));
    }
}
