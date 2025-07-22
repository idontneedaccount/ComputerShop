package com.example.computershop.service.impl;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import com.example.computershop.enums.Role;
import com.example.computershop.repository.NotificationRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Notification createOrderSuccessNotification(User user, Order order) {
        String message = String.format(
                "Đơn hàng #%s của bạn đã được đặt thành công! Tổng tiền: %,d VND. Chúng tôi sẽ xử lý và giao hàng trong thời gian sớm nhất.",
                order.getId().substring(0, 8), order.getTotalAmount());

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .message(message)
                .orderId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public void createNewOrderNotificationForAdmin(Order order) {
        // Lấy tất cả admin users
        List<User> adminUsers = userRepository.findByRole(Role.Admin);

        String customerName = order.getCustomerName() != null ? order.getCustomerName() : "N/A";
        String message = String.format(
                "Có đơn hàng mới #%s từ khách hàng %s. Tổng tiền: %,d VND. Vui lòng kiểm tra và xử lý.",
                order.getId().substring(0, 8), customerName, order.getTotalAmount());

        for (User admin : adminUsers) {
            Notification notification = Notification.builder()
                    .userId(admin.getUserId())
                    .message(message)
                    .orderId(order.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Override
    public void createBroadcastNotification(String message) {
        List<User> allUsers = userRepository.findByIsActive(true);

        for (User user : allUsers) {
            Notification notification = Notification.builder()
                    .userId(user.getUserId())
                    .message(message)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Override
    public Notification createNotificationForUser(String userId, String message, String orderId, String productId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .orderId(orderId)
                .productId(productId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByUserId(String userId) {
        return notificationRepository.findUnreadNotificationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotificationsByUserId(String userId) {
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    public void markAllNotificationsAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> filterNotifications(String search, String status, String type) {
        List<Notification> all = notificationRepository.findAllOrderByCreatedAtDesc();
        return all.stream()
                .filter(n -> search == null || search.isEmpty() ||
                        (n.getMessage() != null && n.getMessage().toLowerCase().contains(search.toLowerCase())))
                .filter(n -> status == null || status.isEmpty() ||
                        ("unread".equals(status) && !Boolean.TRUE.equals(n.getIsRead())) ||
                        ("read".equals(status) && Boolean.TRUE.equals(n.getIsRead())))
                .filter(n -> type == null || type.isEmpty() ||
                        ("order".equals(type) && n.getOrderId() != null) ||
                        ("product".equals(type) && n.getProductId() != null) ||
                        ("system".equals(type) && n.getOrderId() == null && n.getProductId() == null))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteNotificationsOlderThan(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        return notification.orElse(null);
    }

    @Override
    public void deleteNotification(String notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}