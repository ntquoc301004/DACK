package DACK.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookReviewForm {
    @Min(value = 1, message = "Vui lòng chọn số sao từ 1 đến 5")
    @Max(value = 5, message = "Vui lòng chọn số sao từ 1 đến 5")
    private Integer rating;

    @Size(max = 1000, message = "Nội dung đánh giá tối đa 1000 ký tự")
    private String comment;
}
