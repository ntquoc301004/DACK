package DACK.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {
    @NotBlank
    @Email
    @Size(max = 120)
    private String email;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 80)
    private String province;

    @Size(max = 80)
    private String district;

    @Size(max = 80)
    private String ward;

    @Size(max = 255)
    private String streetDetail;
}

