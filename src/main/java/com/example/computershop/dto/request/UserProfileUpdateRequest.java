package com.example.computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {
    
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2-100 ký tự")
    private String fullName;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phoneNumber;
    
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;
    
    // Password fields - only for non-OAuth2 users
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String currentPassword;
    
    @Size(min = 6, max = 100, message = "Mật khẩu mới phải từ 6-100 ký tự")
    private String newPassword;
    
    @Size(min = 6, max = 100, message = "Xác nhận mật khẩu phải từ 6-100 ký tự")
    private String confirmPassword;
    
    // Helper method to check if password change is requested
    public boolean isPasswordChangeRequested() {
        return currentPassword != null && !currentPassword.trim().isEmpty() &&
               newPassword != null && !newPassword.trim().isEmpty() &&
               confirmPassword != null && !confirmPassword.trim().isEmpty();
    }
    
    // Helper method to validate password confirmation
    public boolean isPasswordConfirmed() {
        if (!isPasswordChangeRequested()) {
            return true; // No password change requested, so it's valid
        }
        return newPassword != null && newPassword.equals(confirmPassword);
    }
} 