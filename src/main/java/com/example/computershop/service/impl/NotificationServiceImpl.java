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
    public void createOrderSuccessNotification(User user, Order order) {
        String message = "";
        if(order.getStatus().isEmpty()) {
            message = String.format(
                    "Đơn hàng #%s của bạn đã được đặt thành công! Tổng tiền: %,d VND. Chúng tôi sẽ xử lý và giao hàng trong thời gian sớm nhất.",
                    order.getId().substring(0, 8), order.getTotalAmount());
        }else{
            message = String.format("\"Đơn hàng #%s của bạn đã bị huỷ do không thanh toán!",order.getId().substring(0, 8));
        }
        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .message(message)
                .orderId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void createOrderStatusChangeNotification(Order order, String oldStatus, String newStatus) {
        // Tìm user của đơn hàng
        Optional<User> userOpt = userRepository.findById(order.getUserId());
        if (userOpt.isEmpty()) {
            return;
        }
        
        User user = userOpt.get();
        String message = String.format("Trạng thái đơn hàng #%s đã được cập nhật từ '%s' thành '%s'.", order.getId(), getStatusDisplayName(oldStatus), getStatusDisplayName(newStatus));

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .message(message)
                .orderId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void createNewOrderNotificationForAdmin(Order order) {
        // Lấy tất cả admin users
        List<User> adminUsers = userRepository.findByRole(Role.Admin);
        List<User> salesUsers = userRepository.findByRole(Role.Sales);
        String customerName = order.getCustomerName() != null ? order.getCustomerName() : "N/A";
        String message = String.format(
                "Có đơn hàng mới #%s từ khách hàng %s. Tổng tiền: %,d VND. Vui lòng kiểm tra và xử lý.",
                order.getId().substring(0, 8), customerName, order.getTotalAmount());
        for (User salesUser : salesUsers) {
            Notification notification = Notification.builder()
                    .userId(salesUser.getUserId())
                    .message(message)
                    .orderId(order.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }
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
    public List<Notification> searchNotifications(String search, String status, String type) {
        List<Notification> all = notificationRepository.findAllOrderByCreatedAtDesc();
        
        // Debug logging
        System.out.println("=== SEARCH NOTIFICATIONS DEBUG ===");
        System.out.println("Total notifications in database: " + all.size());
        System.out.println("Search term: " + search);
        System.out.println("Status filter: " + status);
        System.out.println("Type filter: " + type);
        
        List<Notification> filtered = all.stream()
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
        
        // Debug type breakdown
        long orderCount = all.stream().filter(n -> n.getOrderId() != null).count();
        long productCount = all.stream().filter(n -> n.getProductId() != null).count();
        long systemCount = all.stream().filter(n -> n.getOrderId() == null && n.getProductId() == null).count();
        
        System.out.println("Breakdown by type:");
        System.out.println("- Order notifications: " + orderCount);
        System.out.println("- Product notifications: " + productCount);
        System.out.println("- System notifications: " + systemCount);
        System.out.println("After filtering: " + filtered.size() + " notifications");
        System.out.println("=====================================");
        
        return filtered;
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

    private String getStatusDisplayName(String status) {
        if (status == null) return "Không xác định";

        switch (status) {
            case "PENDING":
                return "⏳ Chờ xác nhận";
            case "PAYMENT_PENDING":
                return "💳 Chờ thanh toán";
            case "CONFIRMED":
                return "✅ Đã xác nhận";
            case "PROCESSING":
                return "🔄 Đang xử lý";
            case "SHIPPED":
                return "🚚 Đang giao hàng";
            case "DELIVERED":
                return "📦 Đã giao hàng";
            case "USER_CONFIRMED":
                return "✅ Khách đã nhận";
            case "CANCELLED":
                return "❌ Đã hủy";
            default:
                return "❓ Không xác định";
        }
    }
}