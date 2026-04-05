package DACK.web;

import DACK.model.Book;
import DACK.model.Category;
import DACK.model.OrderStatuses;
import DACK.repo.BookRepository;
import DACK.repo.CategoryRepository;
import DACK.repo.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private static final String[] CATEGORY_ICONS = {"📚", "🎯", "✨", "📖", "🔥", "💫", "🌙", "⚡"};

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping({"/", "/home"})
    public String home(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            Model model
    ) {
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, 12, Sort.by(Sort.Direction.DESC, "id"));
        String keyword = q == null ? "" : q.trim();
        boolean hasKeyword = !keyword.isBlank();
        boolean hasCategory = categoryId != null;

        var books = hasKeyword
                ? (hasCategory
                    ? bookRepository.findByTitleContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable)
                    : bookRepository.findByTitleContainingIgnoreCase(keyword, pageable))
                : (hasCategory
                    ? bookRepository.findByCategoryId(categoryId, pageable)
                    : bookRepository.findAll(pageable));

        var categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        model.addAttribute("books", books);
        model.addAttribute("q", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryImages", categories.stream().collect(java.util.stream.Collectors.toMap(
                c -> c.getId(),
                c -> bookRepository.findTop3ByCategoryIdOrderByIdDesc(c.getId()).stream()
                        .map(b -> b.getImage())
                        .filter(img -> img != null && !img.isBlank())
                        .toList()
        )));
        model.addAttribute("categoryIconById", categoryIcons(categories));
        model.addAttribute("spotlightBooks", bookRepository.findTop3ByOrderByIdDesc());
        var dealBook = bookRepository.findFirstByOrderByIdDesc().orElse(null);
        model.addAttribute("dealBook", dealBook);
        model.addAttribute("promoCategories", categories.stream().limit(4).toList());
        model.addAttribute("soldByBookId", soldByBookIdMap(books.getContent(), dealBook));
        model.addAttribute("homePage", true);
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/news")
    public String news() {
        return "news";
    }

    /** Số lượng đã bán (đơn đã thanh toán / đã duyệt) theo mã sách — dùng trên trang chủ. */
    private Map<Long, Long> soldByBookIdMap(List<Book> pageBooks, Book dealBook) {
        List<Long> ids = pageBooks.stream().map(Book::getId).collect(Collectors.toCollection(ArrayList::new));
        if (dealBook != null) {
            ids.add(dealBook.getId());
        }
        List<Long> unique = ids.stream().distinct().toList();
        Map<Long, Long> map = new HashMap<>();
        if (unique.isEmpty()) {
            return map;
        }
        for (Long id : unique) {
            map.put(id, 0L);
        }
        for (Object[] row : orderDetailRepository.sumSoldQuantityGroupedByBookIdIn(unique, OrderStatuses.REVENUE_AND_SOLD)) {
            map.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private static Map<Long, String> categoryIcons(List<Category> categories) {
        Map<Long, String> map = new HashMap<>();
        for (int i = 0; i < categories.size(); i++) {
            map.put(categories.get(i).getId(), CATEGORY_ICONS[i % CATEGORY_ICONS.length]);
        }
        return map;
    }
}

