package com.example.computershop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class UserCreationRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    String username;
    @Size(min = 8,message = "Mật khẩu cần ít nhất 8 ký tự.")
    String password;
    @Size(min = 8,message = "Mật khẩu cần ít nhất 8 ký tự.")
    String passwordConfirm;
    @NotBlank (message = "Tên không được để trống.")
    String fullName;
    @Email
    @NotBlank
    String email;
    @Pattern(regexp = "^(0)(\\d{9})$", message = "Số điện thoại không hợp lệ.")
    String phoneNumber;


}
