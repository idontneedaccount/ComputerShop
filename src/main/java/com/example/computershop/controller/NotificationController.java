package com.example.computershop.controller;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.User;
import com.example.computershop.service.NotificationService;
import com.example.computershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        // Sử dụng service method thay vì filter
        List<Notification> userNotifications = notificationService.getNotificationsByUserId(user.getUserId());
        long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());
        
        model.addAttribute("notifications", userNotifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);
        
        return "user/notifications";
    }
    
    /**
     * API để lấy số lượng notifications chưa đọc
     */
    @GetMapping("/api/unread-count")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                errorResponse.put("success", false);
                errorResponse.put("unreadCount", 0);
                return ResponseEntity.ok(errorResponse);
            }
            
            // Count unread notifications
            long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("success", false);
            errorResponse.put("unreadCount", 0);
            return ResponseEntity.ok(errorResponse);
        }
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
        
        // Sử dụng service method
        List<Notification> userNotifications = notificationService.getNotificationsByUserId(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", userNotifications);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API để lấy danh sách notifications chưa đọc của user
     */
    @GetMapping("/api/unread-notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        // Sử dụng service method để lấy chỉ notifications chưa đọc
        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsByUserId(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", unreadNotifications);
        response.put("unreadCount", unreadNotifications.size());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Đánh dấu notification đã đọc
     */
    @PostMapping("/mark-read/{notificationId}")
    public String markAsReadWeb(@PathVariable String notificationId, 
                               @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                               Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập");
            return "redirect:/auth/login";
        }
        
        try {
            // Kiểm tra notification có thuộc về user không
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông báo");
                return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
            }
            
            if (!notification.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này");
                return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
            }
            
            // Mark as read
            notificationService.markNotificationAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Đã đánh dấu thông báo đã đọc");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
    }

    /**
     * Đánh dấu notification đã đọc (API version)
     */
    @PostMapping("/api/mark-read/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }
        
        // Kiểm tra notification có thuộc về user không
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
        }
        
        if (!notification.getUserId().equals(user.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
        }
        
        // Mark as read
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
        
        // Mark all notifications as read
        notificationService.markAllNotificationsAsRead(user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    // ===== ADMIN ENDPOINTS =====
    
    /**
     * Hiển thị trang notifications cá nhân của admin
     */
    @GetMapping("/admin/personal")
    @PreAuthorize("hasRole('Admin')")
    public String adminPersonalNotifications(Model model, Principal principal) {
        User admin = getCurrentUser(principal);
        if (admin == null) {
            return "redirect:/auth/login";
        }
        
        // Lấy notifications của admin user
        List<Notification> adminNotifications = notificationService.getNotificationsByUserId(admin.getUserId());
        long unreadCount = notificationService.countUnreadNotificationsByUserId(admin.getUserId());
        
        // Tính toán statistics
        long orderNotifications = adminNotifications.stream()
            .filter(n -> n.getOrderId() != null)
            .count();
        
        long systemNotifications = adminNotifications.stream()
            .filter(n -> n.getOrderId() == null && n.getProductId() == null)
            .count();
        
        model.addAttribute("notifications", adminNotifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("orderNotifications", orderNotifications);
        model.addAttribute("systemNotifications", systemNotifications);
        
        return "admin/notifications";
    }
    
    /**
     * Hiển thị trang quản lý notifications cho admin với search/filter
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('Admin')")
    public String listNotifications(
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "type", required = false) String type,
        Model model, Principal principal) {
        
        User admin = getCurrentUser(principal);
        if (admin == null) {
            return "redirect:/auth/login";
        }
        
        // Chỉ lấy notifications của admin user hiện tại
        List<Notification> allUserNotifications = notificationService.getAllNotifications();
        List<Notification> notifications;
        
        // Áp dụng search và filter trên notifications của user hiện tại
        if ((search != null && !search.trim().isEmpty()) || 
            (status != null && !status.trim().isEmpty()) || 
            (type != null && !type.trim().isEmpty())) {
            // Filter trong notifications của user hiện tại
            notifications = allUserNotifications.stream()
                .filter(n -> {
                    boolean matchesSearch = search == null || search.trim().isEmpty() || 
                        n.getMessage().toLowerCase().contains(search.toLowerCase()) ||
                            (n.getUser() != null && n.getUser().getFullName().toLowerCase().contains(search.toLowerCase()));

                    boolean matchesStatus = status == null || status.trim().isEmpty() || 
                        (status.equals("read") && (n.getIsRead() != null && n.getIsRead())) ||
                        (status.equals("unread") && (n.getIsRead() == null || !n.getIsRead()));
                    
                    boolean matchesType = type == null || type.trim().isEmpty() ||
                        (type.equals("order") && n.getOrderId() != null) ||
                        (type.equals("system") && n.getOrderId() == null && n.getProductId() == null);
                    
                    return matchesSearch && matchesStatus && matchesType;
                })
                .toList();
        } else {
            notifications = allUserNotifications;
        }
        
        // Tính statistics từ notifications của user hiện tại
        long totalNotifications = allUserNotifications.size();
        long unreadNotifications = allUserNotifications.stream()
            .mapToLong(n -> (n.getIsRead() == null || !n.getIsRead()) ? 1 : 0)
            .sum();
        long readNotifications = totalNotifications - unreadNotifications;
        
        // Thêm statistics theo type từ notifications của user hiện tại
        long orderNotifications = allUserNotifications.stream()
            .mapToLong(n -> n.getOrderId() != null ? 1 : 0)
            .sum();
        long systemNotifications = allUserNotifications.stream()
            .mapToLong(n -> (n.getOrderId() == null && n.getProductId() == null) ? 1 : 0)
            .sum();
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("searchTerm", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedType", type);
        model.addAttribute("totalNotifications", totalNotifications);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("readNotifications", readNotifications);
        model.addAttribute("orderNotifications", orderNotifications);
        model.addAttribute("systemNotifications", systemNotifications);
        
        return "admin/notifications/list";
    }
    
    /**
     * Hiển thị form gửi notification broadcast
     */
    @GetMapping("/admin/broadcast")
    @PreAuthorize("hasRole('Admin')")
    public String showBroadcastForm(Model model) {
        // Thêm thống kê users để hiển thị trong form
        long totalUsers = userService.getAll().size();
        long onlineUsers = 0; // Tạm thời set = 0
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("onlineUsers", onlineUsers);
        
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
     * API gửi notification cho chính admin user
     */
    @PostMapping("/admin/send-to-user")
    @PreAuthorize("hasRole('Admin')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendNotificationToUser(
            @RequestParam("userId") String userId,
            @RequestParam("message") String message,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "productId", required = false) String productId,
            Principal principal) {
        
        try {
            User admin = getCurrentUser(principal);
            if (admin == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
            }
            
            // Chỉ cho phép admin gửi notification cho chính mình
            if (!userId.equals(admin.getUserId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "You can only send notifications to yourself"));
            }
            
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
     * Xem chi tiết notification cho admin
     */
    @GetMapping("/admin/detail/{notificationId}")
    public String viewNotificationDetail(@PathVariable String notificationId, Model model, 
                                       Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(principal);
            if (admin == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để truy cập trang này");
                return "redirect:/auth/login";
            }
            
            // Lấy notification
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông báo");
                return "redirect:/notifications/admin";
            }
            
            // Lấy thông tin liên quan
            Map<String, Object> relatedInfo = getDetailedRelatedInfo(notification);
            
            // Tự động đánh dấu đã đọc khi xem detail
            if (notification.getIsRead() == null || !notification.getIsRead()) {
                notificationService.markNotificationAsRead(notificationId);
                // Reload notification để có updated read status
                notification = notificationService.getNotificationById(notificationId);
            }
            
            // Thêm data vào model
            model.addAttribute("notification", notification);
            model.addAttribute("relatedInfo", relatedInfo);
            
            return "admin/notifications/detail";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tải thông tin: " + e.getMessage());
            return "redirect:/notifications/admin";
        }
    }

    /**
     * Lấy thông tin chi tiết liên quan đến notification (cho detail page)
     */
    private Map<String, Object> getDetailedRelatedInfo(Notification notification) {
        Map<String, Object> relatedInfo = new HashMap<>();
        
        try {
            // Thông tin cơ bản
            relatedInfo.put("notificationId", notification.getNotificationId());
            relatedInfo.put("userId", notification.getUserId());
            relatedInfo.put("createdAt", notification.getCreatedAt());
            relatedInfo.put("isRead", notification.getIsRead());
            // relatedInfo.put("readAt", notification.getReadAt()); // Comment out nếu không có field này
            
            // Thông tin về loại notification
            if (notification.getOrderId() != null) {
                relatedInfo.put("type", "order");
                relatedInfo.put("typeDescription", "Thông báo đơn hàng");
                relatedInfo.put("orderId", notification.getOrderId());
                // Có thể thêm thông tin chi tiết về order nếu cần
                relatedInfo.put("orderDetails", "Chi tiết đơn hàng sẽ được hiển thị ở đây");
            } else if (notification.getProductId() != null) {
                relatedInfo.put("type", "product");
                relatedInfo.put("typeDescription", "Thông báo sản phẩm");
                relatedInfo.put("productId", notification.getProductId());
                // Có thể thêm thông tin chi tiết về product nếu cần
                relatedInfo.put("productDetails", "Chi tiết sản phẩm sẽ được hiển thị ở đây");
            } else {
                relatedInfo.put("type", "system");
                relatedInfo.put("typeDescription", "Thông báo hệ thống");
                relatedInfo.put("systemInfo", "Thông báo chung của hệ thống");
            }
            
            // Thông tin về user
            if (notification.getUser() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", notification.getUser().getUserId());
                userInfo.put("fullName", notification.getUser().getFullName());
                userInfo.put("email", notification.getUser().getEmail());
                userInfo.put("role", notification.getUser().getRole());
                relatedInfo.put("userInfo", userInfo);
            }
            
        } catch (Exception e) {
            relatedInfo.put("error", "Không thể tải thông tin liên quan: " + e.getMessage());
        }
        
        return relatedInfo;
    }

    
    // ===== HELPER METHODS =====
    
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        try {
            User user = userService.getUserFromPrincipal(principal);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

}