package DACK.web.admin;

import DACK.model.Book;
import DACK.repo.BookRepository;
import DACK.repo.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/books")
public class AdminBookController {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    private void addAdminLayoutAttrs(Model model, String pageTitle) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("adminDisplayName", auth != null ? auth.getName() : "Admin");
        model.addAttribute("adminPageTitle", pageTitle);
        model.addAttribute("adminNavActive", "books");
    }

    @GetMapping
    public String list(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        int safePage = Math.max(page, 0);
        var books = bookRepository.findAll(PageRequest.of(safePage, 10, Sort.by(Sort.Direction.DESC, "id")));
        model.addAttribute("books", books);
        addAdminLayoutAttrs(model, "Quản lý sách");
        return "admin/books/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Book book = new Book();
        book.setQuantity(0);
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        addAdminLayoutAttrs(model, "Thêm sách");
        return "admin/books/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (book.getCategory() == null || book.getCategory().getId() == null) {
            bindingResult.rejectValue("category", "required", "Vui lòng chọn danh mục");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
            addAdminLayoutAttrs(model, "Thêm sách");
            return "admin/books/form";
        }
        var cat = categoryRepository.findById(book.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
        book.setCategory(cat);
        bookRepository.save(book);
        redirectAttributes.addFlashAttribute("flash", "Thêm sách thành công");
        return "redirect:/admin/books";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookRepository.findById(id).orElseThrow());
        model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
        addAdminLayoutAttrs(model, "Sửa sách");
        return "admin/books/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (book.getCategory() == null || book.getCategory().getId() == null) {
            bindingResult.rejectValue("category", "required", "Vui lòng chọn danh mục");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll(Sort.by("name")));
            addAdminLayoutAttrs(model, "Sửa sách");
            return "admin/books/form";
        }
        var existing = bookRepository.findById(id).orElseThrow();
        var cat = categoryRepository.findById(book.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setPrice(book.getPrice());
        existing.setQuantity(book.getQuantity());
        existing.setImage(book.getImage());
        existing.setDescription(book.getDescription());
        existing.setCategory(cat);

        bookRepository.save(existing);
        redirectAttributes.addFlashAttribute("flash", "Cập nhật sách thành công");
        return "redirect:/admin/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("flash", "Xóa sách thành công");
        return "redirect:/admin/books";
    }
}

