package DACK.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String author;

    @NotNull(message = "Vui lòng nhập giá")
    @PositiveOrZero(message = "Giá phải từ 0 trở lên")
    @Digits(integer = 10, fraction = 0, message = "Giá (VNĐ) chỉ nhập số nguyên, không dấu phẩy/chấm (ví dụ 85000)")
    @DecimalMax(value = "9999999999", message = "Giá vượt quá mức cho phép (tối đa 9.999.999.999 đ)")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 500)
    @Column(length = 500)
    private String image;

    @Lob
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}

