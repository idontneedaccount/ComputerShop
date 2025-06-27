package com.example.computershop.controller;

import com.example.computershop.dto.UserProfileData;
import com.example.computershop.dto.request.UserProfileUpdateRequest;
import com.example.computershop.entity.Order;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
            
            // Thêm empty form object để tránh lỗi validation trong template
            model.addAttribute("userProfileUpdateRequest", new UserProfileUpdateRequest());
            
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
    public String updateProfile(@Valid @ModelAttribute UserProfileUpdateRequest request, 
                               BindingResult bindingResult,
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate form data
            if (bindingResult.hasErrors()) {
                // Get current user data for redisplay
                UserProfileData profileData = userService.getCurrentUser();
                model.addAttribute("user", profileData.getUser());
                model.addAttribute("isOAuth2", profileData.isOAuth2());
                model.addAttribute("authProvider", profileData.getAuthProvider());
                model.addAttribute("hasPhoneNumber", profileData.hasPhoneNumber());
                model.addAttribute("hasAddress", profileData.hasAddress());
                
                // Add orders for complete profile display
                List<Order> userOrders = orderService.getOrdersByUserWithDetails(profileData.getUser().getUserId());
                model.addAttribute("orders", userOrders);
                model.addAttribute("hasOrders", !userOrders.isEmpty());
                
                model.addAttribute("error", "Vui lòng kiểm tra lại thông tin đã nhập.");
                return "user/userprofile";
            }
            
            // Validate password confirmation if password change is requested
            if (request.isPasswordChangeRequested() && !request.isPasswordConfirmed()) {
                UserProfileData profileData = userService.getCurrentUser();
                model.addAttribute("user", profileData.getUser());
                model.addAttribute("isOAuth2", profileData.isOAuth2());
                model.addAttribute("authProvider", profileData.getAuthProvider());
                model.addAttribute("hasPhoneNumber", profileData.hasPhoneNumber());
                model.addAttribute("hasAddress", profileData.hasAddress());
                
                List<Order> userOrders = orderService.getOrdersByUserWithDetails(profileData.getUser().getUserId());
                model.addAttribute("orders", userOrders);
                model.addAttribute("hasOrders", !userOrders.isEmpty());
                
                model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
                return "user/userprofile";
            }
            
            // Update profile through service
            String result = userService.updateProfile(request);
            
            if (result.equals("SUCCESS")) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
                log.info("Profile updated successfully for user");
            } else {
                redirectAttributes.addFlashAttribute("error", result);
                log.warn("Profile update failed: {}", result);
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