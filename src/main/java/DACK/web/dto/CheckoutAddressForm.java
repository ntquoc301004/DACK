package DACK.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutAddressForm {

    @NotBlank(message = "Vui lòng nhập họ tên người nhận")
    @Size(max = 120)
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Vui lòng chọn Tỉnh/Thành phố")
    @Size(max = 80)
    private String province;

    @NotBlank(message = "Vui lòng nhập Quận/Huyện")
    @Size(max = 80)
    private String district;

    @NotBlank(message = "Vui lòng nhập Phường/Xã")
    @Size(max = 80)
    private String ward;

    @NotBlank(message = "Vui lòng nhập địa chỉ (số nhà, đường)")
    @Size(max = 255)
    private String streetDetail;

    @Size(max = 500)
    private String note;

    private boolean giftBox;

    private boolean giftRibbon;

    private boolean greetingCard;

    @Size(max = 300, message = "Loi nhan qua tang toi da 300 ky tu")
    private String giftMessage;
}
