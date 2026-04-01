package DACK.service;

import DACK.web.dto.ContactFormDto;
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
public class ContactMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${contact.inbox:}")
    private String inboxAddress;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public ContactMailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /**
     * Gửi nội dung form tới hộp thư quản trị. Trả về false nếu chưa cấu hình hoặc lỗi gửi.
     */
    public boolean sendContactMessage(ContactFormDto form) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Chưa cấu hình SMTP (spring.mail.*) — không gửi được email");
            return false;
        }
        if (inboxAddress == null || inboxAddress.isBlank()) {
            log.warn("contact.inbox chưa được cấu hình — không gửi được email liên hệ");
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
            helper.setTo(inboxAddress);
            helper.setReplyTo(form.getEmail(), form.getFullName());
            helper.setSubject("[ComicStore] Liên hệ từ " + form.getFullName());
            String body = """
                    Họ và tên: %s
                    Email: %s
                    Điện thoại: %s

                    Nội dung:
                    %s
                    """.formatted(
                    form.getFullName(),
                    form.getEmail(),
                    form.getPhone(),
                    form.getMessage()
            );
            helper.setText(body, false);
            mailSender.send(mime);
            return true;
        } catch (Exception e) {
            log.error("Không gửi được email liên hệ", e);
            return false;
        }
    }
}
