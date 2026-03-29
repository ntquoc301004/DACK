package DACK.service;

import DACK.model.PasswordResetToken;
import DACK.model.User;
import DACK.repo.PasswordResetTokenRepository;
import DACK.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int TOKEN_VALID_HOURS = 1;

    public record IssuedResetToken(String token, String recipientEmail) {}

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tạo token mới cho email đã đăng ký; không làm gì nếu không có tài khoản.
     */
    @Transactional
    public Optional<IssuedResetToken> issueResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        tokenRepository.deleteByUser(user);

        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(tokenValue);
        entity.setUser(user);
        entity.setExpiry(Instant.now().plus(TOKEN_VALID_HOURS, ChronoUnit.HOURS));
        tokenRepository.save(entity);

        return Optional.of(new IssuedResetToken(tokenValue, user.getEmail()));
    }

    @Transactional(readOnly = true)
    public boolean isTokenValid(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }
        return tokenRepository.findByToken(rawToken.trim())
                .filter(t -> t.getExpiry().isAfter(Instant.now()))
                .isPresent();
    }

    @Transactional
    public boolean resetPassword(String rawToken, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(rawToken == null ? "" : rawToken.trim());
        if (opt.isEmpty()) {
            return false;
        }
        PasswordResetToken prt = opt.get();
        if (!prt.getExpiry().isAfter(Instant.now())) {
            tokenRepository.delete(prt);
            return false;
        }
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByUser(user);
        return true;
    }
}
