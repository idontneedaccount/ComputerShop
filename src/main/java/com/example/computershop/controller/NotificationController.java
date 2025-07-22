package com.example.computershop.controller;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.User;
import com.example.computershop.service.NotificationService;
import com.example.computershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    
    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
    
    // ===== USER ENDPOINTS =====
    
    /**
     * Hiển thị trang notifications của user
     */
    @GetMapping("/user")
    public String userNotifications(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(user.getUserId());
        long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        
        return "user/notifications";
    }
    
    /**
     * API để lấy số lượng notifications chưa đọc
     */
    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API để lấy danh sách notifications của user
     */
    @GetMapping("/api/user-notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đánh dấu notification đã đọc
     */
    @PostMapping("/mark-read/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        // Kiểm tra notification có thuộc về user không
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null || !notification.getUserId().equals(user.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification not found or access denied"));
        }
        
        notificationService.markNotificationAsRead(notificationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đánh dấu tất cả notifications đã đọc
     */
    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        notificationService.markAllNotificationsAsRead(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    // ===== ADMIN ENDPOINTS =====
    
    /**
     * Hiển thị trang quản lý notifications cho admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('Admin')")
    public String adminNotifications(Model model) {
        List<Notification> allNotifications = notificationService.getAllNotifications();
        model.addAttribute("notifications", allNotifications);
        return "admin/notifications/list";
    }
    
    /**
     * Hiển thị form gửi notification broadcast
     */
    @GetMapping("/admin/broadcast")
    @PreAuthorize("hasRole('Admin')")
    public String showBroadcastForm() {
        return "admin/notifications/broadcast";
    }
    
    /**
     * Gửi notification đến tất cả users
     */
    @PostMapping("/admin/broadcast")
    @PreAuthorize("hasRole('Admin')")
    public String sendBroadcastNotification(@RequestParam("message") String message, 
                                          RedirectAttributes redirectAttributes) {
        try {
            if (message == null || message.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nội dung thông báo không được để trống");
                return "redirect:/notifications/admin/broadcast";
            }
            
            notificationService.createBroadcastNotification(message.trim());
            redirectAttributes.addFlashAttribute("success", "Đã gửi thông báo đến tất cả người dùng thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi gửi thông báo: " + e.getMessage());
        }
        
        return "redirect:/notifications/admin";
    }
    
    /**
     * API gửi notification cho user cụ thể
     */
    @PostMapping("/admin/send-to-user")
    @PreAuthorize("hasRole('Admin')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendNotificationToUser(
            @RequestParam("userId") String userId,
            @RequestParam("message") String message,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "productId", required = false) String productId) {
        
        try {
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }
            
            Notification notification = notificationService.createNotificationForUser(
                userId, message.trim(), orderId, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification sent successfully");
            response.put("notification", notification);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa notification (admin only)
     */
    @DeleteMapping("/admin/{notificationId}")
    @PreAuthorize("hasRole('Admin')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable String notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Notification deleted successfully");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to delete notification: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa notifications cũ (admin only)
     */
    @PostMapping("/admin/cleanup")
    @PreAuthorize("hasRole('Admin')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(@RequestParam("daysOld") int daysOld) {
        try {
            if (daysOld < 7) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete notifications newer than 7 days"));
            }
            
            notificationService.deleteOldNotifications(daysOld);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Old notifications cleaned up successfully");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to cleanup notifications: " + e.getMessage()));
        }
    }
    
    @GetMapping("/notifications/admin")
    @PreAuthorize("hasRole('Admin')")
    public String listNotifications(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            Model model
    ) {
        List<Notification> notifications = notificationService.filterNotifications(search, status, type);
        System.out.println("Notifications size: " + notifications.size());
        model.addAttribute("notifications", notifications);
        model.addAttribute("searchTerm", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        return "admin/notifications/list";
    }
    
    // ===== HELPER METHODS =====
    
    private User getCurrentUser(Principal principal) {
        return userService.getUserFromPrincipal(principal);
    }
} 