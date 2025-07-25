package com.example.computershop.service;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationService {
    void createOrderSuccessNotification(User user, Order order);

    void createNewOrderNotificationForAdmin(Order order);

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

    // Tìm notification theo ID
    Notification getNotificationById(String notificationId);

    // Xóa notification
    void deleteNotification(String notificationId);

    void createOrderStatusChangeNotification(Order order, String oldStatus, String newStatus);

    List<Notification> searchNotifications(String search, String status, String type);
}