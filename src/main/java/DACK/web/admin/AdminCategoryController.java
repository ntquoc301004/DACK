package DACK.web.admin;

import DACK.model.Category;
import DACK.repo.BookRepository;
import DACK.repo.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    private void addAdminLayoutAttrs(Model model, String pageTitle) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("adminDisplayName", auth != null ? auth.getName() : "Admin");
        model.addAttribute("adminPageTitle", pageTitle);
        model.addAttribute("adminNavActive", "categories");
    }

    @GetMapping
    public String list(Model model) {
        var categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        model.addAttribute("categories", categories);
        model.addAttribute("bookCountByCategoryId", categories.stream().collect(Collectors.toMap(
                Category::getId,
                c -> bookRepository.countByCategoryId(c.getId())
        )));
        addAdminLayoutAttrs(model, "Quản lý danh mục");
        return "admin/categories/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        addAdminLayoutAttrs(model, "Thêm danh mục");
        return "admin/categories/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        checkDuplicateName(category.getName(), null, bindingResult);
        if (bindingResult.hasErrors()) {
            addAdminLayoutAttrs(model, "Thêm danh mục");
            return "admin/categories/form";
        }
        category.setName(category.getName().trim());
        categoryRepository.save(category);
        redirectAttributes.addFlashAttribute("flash", "Thêm danh mục thành công");
        return "redirect:/admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryRepository.findById(id).orElseThrow());
        addAdminLayoutAttrs(model, "Sửa danh mục");
        return "admin/categories/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        checkDuplicateName(category.getName(), id, bindingResult);
        if (bindingResult.hasErrors()) {
            category.setId(id);
            addAdminLayoutAttrs(model, "Sửa danh mục");
            return "admin/categories/form";
        }
        var existing = categoryRepository.findById(id).orElseThrow();
        existing.setName(category.getName().trim());
        categoryRepository.save(existing);
        redirectAttributes.addFlashAttribute("flash", "Cập nhật danh mục thành công");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        long cnt = bookRepository.countByCategoryId(id);
        if (cnt > 0) {
            redirectAttributes.addFlashAttribute("flash", "Không xóa được: còn " + cnt + " đầu sách thuộc danh mục này");
            return "redirect:/admin/categories";
        }
        categoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("flash", "Đã xóa danh mục");
        return "redirect:/admin/categories";
    }

    private void checkDuplicateName(String rawName, Long excludeId, BindingResult bindingResult) {
        if (rawName == null || rawName.isBlank()) {
            return;
        }
        String name = rawName.trim();
        categoryRepository.findByNameIgnoreCase(name).ifPresent(found -> {
            if (excludeId == null || !found.getId().equals(excludeId)) {
                bindingResult.rejectValue("name", "duplicate", "Tên danh mục đã tồn tại");
            }
        });
    }
}
