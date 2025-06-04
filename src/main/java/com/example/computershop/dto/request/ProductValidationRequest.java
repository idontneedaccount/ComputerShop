package com.example.computershop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductValidationRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống.")
    String name;

    @NotBlank(message = "Mô tả không được để trống.")
    String description;

    @NotBlank(message = "Hãng sản phẩm không được để trống.")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Hãng sản phẩm chỉ được chứa chữ cái và khoảng trắng.")
    String brand;

    @NotNull(message = "Giá không được để trống.")
    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0.")
    BigInteger price;

    @NotNull(message = "Số lượng không được để trống.")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0.")
    Integer quantity;
} 