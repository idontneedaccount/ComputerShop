package com.example.computershop.service;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    
    // Tạo notification cho user khi đặt hàng thành công
    Notification createOrderSuccessNotification(User user, Order order);
    
    // Tạo notification cho admin khi có đơn hàng mới
    void createNewOrderNotificationForAdmin(Order order);
    
    // Tạo notification cho tất cả users (admin broadcast)
    void createBroadcastNotification(String message);
    
    // Tạo notification cho user cụ thể
    Notification createNotificationForUser(String userId, String message, String orderId, String productId);
    
    // Lấy tất cả notifications của user
    List<Notification> getNotificationsByUserId(String userId);
    
    // Lấy notifications chưa đọc của user
    List<Notification> getUnreadNotificationsByUserId(String userId);
    
    // Đếm số notifications chưa đọc của user
    long countUnreadNotificationsByUserId(String userId);
    
    // Đánh dấu notification đã đọc
    void markNotificationAsRead(String notificationId);
    
    // Đánh dấu tất cả notifications của user đã đọc
    void markAllNotificationsAsRead(String userId);
    
    // Lấy tất cả notifications (dành cho admin)
    List<Notification> getAllNotifications();
    
    // Xóa notifications cũ
    void deleteOldNotifications(int daysOld);
    
    // Tìm notification theo ID
    Notification getNotificationById(String notificationId);
    
    // Xóa notification
    void deleteNotification(String notificationId);

    List<Notification> filterNotifications(String search, String status, String type);
} 