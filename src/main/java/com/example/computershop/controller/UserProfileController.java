package com.example.computershop.controller;

import com.example.computershop.dto.UserProfileData;
import com.example.computershop.dto.request.UserInfoUpdateRequest;
import com.example.computershop.dto.request.PasswordChangeRequest;
import com.example.computershop.entity.Order;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    
    private final UserService userService;
    private final OrderService orderService;
    
    @GetMapping("/user-profile")
    public String showUserProfile(Model model) {
        try {
            // 🎯 Service xử lý tất cả logic, Controller chỉ việc display
            UserProfileData profileData = userService.getCurrentUser();
            
            // Thêm thông tin vào model
            model.addAttribute("user", profileData.getUser());
            model.addAttribute("isOAuth2", profileData.isOAuth2());
            model.addAttribute("authProvider", profileData.getAuthProvider());
            model.addAttribute("hasPhoneNumber", profileData.hasPhoneNumber());
            model.addAttribute("hasAddress", profileData.hasAddress());
            
            // Thêm empty form objects để tránh lỗi validation trong template
            model.addAttribute("userInfoUpdateRequest", new UserInfoUpdateRequest());
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
            
            // Thêm lịch sử đặt hàng
            List<Order> userOrders = orderService.getOrdersByUserWithDetails(profileData.getUser().getUserId());
            model.addAttribute("orders", userOrders);
            model.addAttribute("hasOrders", !userOrders.isEmpty());
            
    
            if (profileData.isOAuth2()) {
                model.addAttribute("oauth2Attributes", profileData.getOauth2Attributes());
                model.addAttribute("oauth2Authorities", profileData.getOauth2Authorities());
            }
            
            log.info("User profile loaded successfully for: {}", profileData.getDisplayName());
            return "user/userprofile";
            
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "user/userprofile";
        } catch (Exception e) {
            log.error("Unexpected error loading user profile", e);
            model.addAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
            return "user/userprofile";
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("action") String action,
                               @ModelAttribute UserInfoUpdateRequest userInfoRequest, 
                               @ModelAttribute PasswordChangeRequest passwordRequest,
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        try {
            String result;
            
            if ("update-info".equals(action)) {
                // Xử lý cập nhật thông tin cá nhân
                result = userService.updateUserInfo(userInfoRequest);
                
                if (result.equals("SUCCESS")) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
                    log.info("User info updated successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", result);
                    log.warn("User info update failed: {}", result);
                }
                
            } else if ("change-password".equals(action)) {
                // Validate password confirmation
                if (!passwordRequest.isPasswordConfirmed()) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
                    return "redirect:/user/user-profile";
                }
                
                // Xử lý đổi mật khẩu
                result = userService.changePassword(passwordRequest);
                
                if (result.equals("SUCCESS")) {
                    redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
                    log.info("Password changed successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", result);
                    log.warn("Password change failed: {}", result);
                }
                
            } else {
                redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ.");
                log.warn("Invalid action: {}", action);
            }
            
        } catch (IllegalStateException e) {
            log.error("Authentication error during profile update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during profile update", e);
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
        }
        
        return "redirect:/user/user-profile";
    }
} 