package DACK.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PasswordResetMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public PasswordResetMailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * Gửi link đặt lại mật khẩu tới email người dùng. Trả về false nếu chưa cấu hình SMTP hoặc lỗi gửi.
     */
    public boolean sendResetLink(String toEmail, String resetUrl) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Chưa cấu hình SMTP (spring.mail.*) — không gửi được email đặt lại mật khẩu");
            return false;
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            log.warn("spring.mail.username chưa được cấu hình — không gửi được email");
            return false;
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("[ComicStore] Đặt lại mật khẩu");
            String body = """
                    Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản ComicStore.

                    Mở liên kết sau (có hiệu lực trong 1 giờ):
                    %s

                    Nếu bạn không yêu cầu, hãy bỏ qua email này.
                    """.formatted(resetUrl);
            helper.setText(body, false);
            mailSender.send(mime);
            return true;
        } catch (Exception e) {
            log.error("Không gửi được email đặt lại mật khẩu", e);
            return false;
        }
    }
}
