package com.example.computershop.controller;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.Payment;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;
    private PaymentRepository paymentRepository;
    
    // Constants - theo pattern ProductController
    private static final String ORDER = "order";
    private static final String ORDERS = "orders";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String STATUS_OPTIONS = "statusOptions";
    private static final String SELECTED_STATUS = "selectedStatus";
    private static final String SELECTED_SORT = "selectedSort";
    private static final String SEARCH_TERM = "searchTerm";
    
    // View constants
    private static final String ORDER_VIEW = "admin/orders/list";
    private static final String ORDER_VIEW_REDIRECT = "redirect:/admin/orders";
    private static final String ORDER_DETAIL_VIEW = "admin/orders/detail";
    
    // Status constants
    private static final List<String> ALL_STATUSES = Arrays.asList(
        "PENDING", "PAYMENT_PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "USER_CONFIRMED", "CANCELLED"
    );

    /**
     * Hiển thị danh sách tất cả đơn hàng với search và sort
     */
    @GetMapping("/orders")
    public String index(Model model,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "sort", required = false, defaultValue = "date_desc") String sort) {
        
        try {
            // Lấy danh sách đơn hàng
            List<Order> orderList = getAllFilteredAndSortedOrders(status, search, sort);
            
            // Lookup payment status cho mỗi order
            Map<String, String> paymentStatusMap = getPaymentStatusMap(orderList);
            
            // Set model attributes
            model.addAttribute(ORDERS, orderList);
            model.addAttribute("paymentStatusMap", paymentStatusMap);
            model.addAttribute(STATUS_OPTIONS, ALL_STATUSES);
            model.addAttribute(SELECTED_STATUS, status != null ? status : "");
            model.addAttribute(SELECTED_SORT, sort);
            model.addAttribute(SEARCH_TERM, search != null ? search : "");
            
            // Thêm thống kê đơn giản
            model.addAttribute("totalOrders", orderList.size());
            
            return ORDER_VIEW;
        } catch (Exception e) {
            model.addAttribute(ERROR, "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute(ORDERS, List.of());
            model.addAttribute(STATUS_OPTIONS, ALL_STATUSES);
            return ORDER_VIEW;
        }
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/orders/{orderId}")
    public String viewOrder(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Không tìm thấy đơn hàng với ID: " + orderId);
                return ORDER_VIEW_REDIRECT;
            }
            

            model.addAttribute(ORDER, order);
            model.addAttribute(STATUS_OPTIONS, ALL_STATUSES);
            return ORDER_DETAIL_VIEW;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Có lỗi xảy ra khi tải chi tiết đơn hàng: " + e.getMessage());
            return ORDER_VIEW_REDIRECT;
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam(value = "orderId", required = false) String orderId,
                                   @RequestParam(value = "status", required = false) String newStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validate parameters
            if (orderId == null || orderId.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR, "ID đơn hàng không được để trống!");
                return ORDER_VIEW_REDIRECT;
            }
            
            if (newStatus == null || newStatus.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(ERROR, "Vui lòng chọn trạng thái mới!");
                return "redirect:/admin/orders/" + orderId;
            }
            
            // Validate status
            if (!ALL_STATUSES.contains(newStatus)) {
                redirectAttributes.addFlashAttribute(ERROR, "Trạng thái không hợp lệ: " + newStatus);
                return "redirect:/admin/orders/" + orderId;
            }
            
            // Get và update order
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Không tìm thấy đơn hàng với ID: " + orderId);
                return ORDER_VIEW_REDIRECT;
            }
            
            String oldStatus = order.getStatus();
            order.setStatus(newStatus);
            
            Order updatedOrder = orderService.updateOrder(order);
            if (updatedOrder != null) {
                String newStatusDisplay = getStatusDisplayName(newStatus);
                redirectAttributes.addFlashAttribute(SUCCESS, 
                    String.format("✅ Đã cập nhật trạng thái đơn hàng thành %s thành công! Thông báo đã được gửi đến khách hàng.", newStatusDisplay));
            } else {
                redirectAttributes.addFlashAttribute(ERROR, "Không thể cập nhật trạng thái đơn hàng");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ERROR, "Có lỗi xảy ra khi cập nhật trạng thái: " + e.getMessage());
        }
        
        // Redirect về trang chi tiết đơn hàng thay vì danh sách
        return "redirect:/admin/orders/" + orderId;
    }



    // =========================== HELPER METHODS ===========================

    /**
     * Lấy danh sách đơn hàng đã filter và sort
     */
    private List<Order> getAllFilteredAndSortedOrders(String status, String search, String sort) {
        List<Order> orders;
        
        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        
        // Search filter
        if (search != null && !search.trim().isEmpty()) {
            orders = filterOrdersBySearch(orders, search.trim().toLowerCase());
        }
        
        // Sort orders
        orders = sortOrders(orders, sort);
        
        return orders;
    }

    /**
     * Filter orders by search term
     */
    private List<Order> filterOrdersBySearch(List<Order> orders, String searchTerm) {
        return orders.stream()
            .filter(order -> {
                // Search in order ID
                if (order.getId() != null && order.getId().toLowerCase().contains(searchTerm)) {
                    return true;
                }
                // ✅ FIXED - Search in customer name using helper method
                if (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(searchTerm)) {
                    return true;
                }
                // ✅ FIXED - Search in email using helper method
                if (order.getCustomerEmail() != null && order.getCustomerEmail().toLowerCase().contains(searchTerm)) {
                    return true;
                }
                // ✅ FIXED - Search in phone using helper method
                if (order.getCustomerPhone() != null && order.getCustomerPhone().contains(searchTerm)) {
                    return true;
                }
                // ✅ NEW - Search in alternative receiver name
                if (order.getAlternativeReceiverName() != null && order.getAlternativeReceiverName().toLowerCase().contains(searchTerm)) {
                    return true;
                }
                // ✅ NEW - Search in alternative receiver phone
                if (order.getAlternativeReceiverPhone() != null && order.getAlternativeReceiverPhone().contains(searchTerm)) {
                    return true;
                }
                return false;
            })
            .collect(Collectors.toList());
    }

    /**
     * Sort orders based on criteria
     */
    private List<Order> sortOrders(List<Order> orders, String sortBy) {
        switch (sortBy) {
            case "date_asc":
                return orders.stream()
                    .sorted((o1, o2) -> {
                        if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                        if (o1.getOrderDate() == null) return 1;
                        if (o2.getOrderDate() == null) return -1;
                        return o1.getOrderDate().compareTo(o2.getOrderDate());
                    })
                    .collect(Collectors.toList());

            case "date_desc":
                return orders.stream()
                    .sorted((o1, o2) -> {
                        if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                        if (o1.getOrderDate() == null) return 1;
                        if (o2.getOrderDate() == null) return -1;
                        return o2.getOrderDate().compareTo(o1.getOrderDate());
                    })
                    .collect(Collectors.toList());

            case "price_asc":
                return orders.stream()
                    .sorted((o1, o2) -> {
                        Long amount1 = o1.getTotalAmount() != null ? o1.getTotalAmount() : 0L;
                        Long amount2 = o2.getTotalAmount() != null ? o2.getTotalAmount() : 0L;
                        return amount1.compareTo(amount2);
                    })
                    .collect(Collectors.toList());

            case "price_desc":
                return orders.stream()
                    .sorted((o1, o2) -> {
                        Long amount1 = o1.getTotalAmount() != null ? o1.getTotalAmount() : 0L;
                        Long amount2 = o2.getTotalAmount() != null ? o2.getTotalAmount() : 0L;
                        return amount2.compareTo(amount1);
                    })
                    .collect(Collectors.toList());

            case "status":
                return orders.stream()
                    .sorted((o1, o2) -> {
                        String status1 = o1.getStatus() != null ? o1.getStatus() : "";
                        String status2 = o2.getStatus() != null ? o2.getStatus() : "";
                        return status1.compareTo(status2);
                    })
                    .collect(Collectors.toList());
                default:
                    return List.of();
        }
    }

    /**
     * Lấy payment status map cho danh sách orders
     */
    private Map<String, String> getPaymentStatusMap(List<Order> orders) {
        Map<String, String> paymentStatusMap = new java.util.HashMap<>();
        
        for (Order order : orders) {
            try {
                // Try to find payment by orderId
                Optional<Payment> paymentOpt = paymentRepository.findByOrderId(UUID.fromString(order.getId()));
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    paymentStatusMap.put(order.getId(), payment.getPaymentStatus());
                } else {
                    // No payment record found, determine status by payment method
                    if ("VNPAY".equals(order.getPaymentMethod())) {
                        paymentStatusMap.put(order.getId(), "PENDING");
                    } else if ("COD".equals(order.getPaymentMethod())) {
                        paymentStatusMap.put(order.getId(), "COD");
                    } else {
                        paymentStatusMap.put(order.getId(), "UNKNOWN");
                    }
                }
            } catch (Exception e) {
                // Handle UUID parsing error or other exceptions
                paymentStatusMap.put(order.getId(), "ERROR");
            }
        }
        
        return paymentStatusMap;
    }
    
    /**
     * Chuyển đổi status code thành tên hiển thị tiếng Việt
     */
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