package DACK.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactFormDto {

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(max = 120)
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 30)
    @Pattern(regexp = "^[0-9+().\\-\\s]{8,30}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Vui lòng nhập nội dung")
    @Size(max = 4000)
    private String message;
}
