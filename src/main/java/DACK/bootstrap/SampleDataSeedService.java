package DACK.bootstrap;

import DACK.model.Book;
import DACK.model.Category;
import DACK.model.Role;
import DACK.model.RoleName;
import DACK.model.User;
import DACK.repo.BookRepository;
import DACK.repo.CategoryRepository;
import DACK.repo.RoleRepository;
import DACK.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Nạp dữ liệu mẫu một lần (idempotent): role, user admin/khách, danh mục, sách.
 * Bật/tắt: {@code app.sample-data.enabled} (mặc định true).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SampleDataSeedService {

    @Value("${app.sample-data.enabled:true}")
    private boolean sampleDataEnabled;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedIfEnabled() {
        if (!sampleDataEnabled) {
            log.debug("Sample data seed skipped (app.sample-data.enabled=false).");
            return;
        }
        seedRoles();
        seedUsers();
        seedBooks();
        log.info("Sample data ready: roles, users (admin/customer), categories & books.");
    }

    private void seedRoles() {
        for (RoleName name : RoleName.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                Role r = new Role();
                r.setName(name);
                roleRepository.save(r);
                log.info("Created role {}", name);
            }
        }
    }

    private void seedUsers() {
        ensureUser("admin", "admin@dack.local", "Quản trị viên", "admin123", RoleName.ADMIN);
        ensureUser("customer", "customer@dack.local", "Khách hàng mẫu", "customer123", RoleName.CUSTOMER);
    }

    private void ensureUser(String username, String email, String fullName, String rawPassword, RoleName roleName) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.getRoles().add(role);
        userRepository.save(u);
        log.info("Created user '{}' (role {})", username, roleName);
    }

    private void seedBooks() {
        if (bookRepository.count() > 0) {
            return;
        }
        Category comic = ensureCategory("Truyện tranh");
        Category novel = ensureCategory("Tiểu thuyết");
        Category science = ensureCategory("Khoa học");

        saveBook("One Piece tập 1", "Eiichiro Oda", 55000, 120, comic,
                "Hành trình của Luffy và băng Mũ Rơm.", "/images/logo.png");
        saveBook("Spy x Family tập 1", "Tatsuya Endo", 48000, 80, comic,
                "Gia đình làm nhiệm vụ đặc biệt.", null);
        saveBook("Nhà giả kim", "Paulo Coelho", 89000, 50, novel,
                "Hành trình theo đuổi ước mơ.", null);
        saveBook("Sapiens", "Yuval Noah Harari", 199000, 30, science,
                "Lược sử loài người.", null);
        saveBook("Vũ trụ trong vỏ hạt dẻ", "Stephen Hawking", 165000, 25, science, null, null);
    }

    private Category ensureCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    return categoryRepository.save(c);
                });
    }

    private void saveBook(String title, String author, int priceVnd, int qty, Category category,
                          String description, String image) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setPrice(BigDecimal.valueOf(priceVnd));
        b.setQuantity(qty);
        b.setCategory(category);
        b.setDescription(description);
        b.setImage(image);
        bookRepository.save(b);
    }
}
