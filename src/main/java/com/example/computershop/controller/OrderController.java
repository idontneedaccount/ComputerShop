package com.example.computershop.controller;

import com.example.computershop.entity.Order;
import com.example.computershop.exception.OrderConstants;
import com.example.computershop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {
    
    // Dependencies
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // =========================== HELPER METHODS ===========================

    /**
     * Get available status transitions for current order status
     */
    private List<String> getAvailableStatusTransitions(String currentStatus) {
        List<String> transitions = new ArrayList<>();
        
        switch (currentStatus) {
            case OrderConstants.STATUS_PENDING:
                transitions.add(OrderConstants.STATUS_CONFIRMED);
                transitions.add(OrderConstants.STATUS_CANCELLED);
                break;
            case OrderConstants.STATUS_CONFIRMED:
                transitions.add(OrderConstants.STATUS_PROCESSING);
                transitions.add(OrderConstants.STATUS_CANCELLED);
                break;
            case OrderConstants.STATUS_PROCESSING:
                transitions.add(OrderConstants.STATUS_SHIPPED);
                transitions.add(OrderConstants.STATUS_CANCELLED);
                break;
            case OrderConstants.STATUS_SHIPPED:
                transitions.add(OrderConstants.STATUS_DELIVERED);
                break;
            case OrderConstants.STATUS_DELIVERED:
                // No further transitions
                break;
            default:
                // Unknown status - no transitions available
                break;
        }
        
        return transitions;
    }

    /**
     * Validate if status transition is allowed
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        List<String> validTransitions = getAvailableStatusTransitions(fromStatus);
        return validTransitions.contains(toStatus);
    }

    // =========================== ORDER LISTING & FILTERING ===========================

    /**
     * List all orders with optional status filtering
     */
    @GetMapping
    public String listAllOrders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
            model.addAttribute(OrderConstants.SELECTED_STATUS, status);
        } else {
            orders = orderService.getAllOrders();
        }
        model.addAttribute(OrderConstants.ORDERS, orders);
        
        // Add status options for filtering
        List<String> statusOptions = List.of(
            OrderConstants.STATUS_PENDING, 
            OrderConstants.STATUS_CONFIRMED, 
            OrderConstants.STATUS_PROCESSING, 
            OrderConstants.STATUS_SHIPPED, 
            OrderConstants.STATUS_DELIVERED, 
            OrderConstants.STATUS_CANCELLED
        );
        model.addAttribute(OrderConstants.STATUS_OPTIONS, statusOptions);
        
        return "admin/orders/list";
    }

    // =========================== ORDER DETAILS & STATUS MANAGEMENT ===========================

    /**
     * View detailed information of a specific order
     */
    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order != null) {
                model.addAttribute(OrderConstants.ORDER, order);
                
                // Add available status transitions
                List<String> availableStatuses = getAvailableStatusTransitions(order.getStatus());
                model.addAttribute(OrderConstants.AVAILABLE_STATUSES, availableStatuses);
                
                return "admin/orders/details";
            } else {
                redirectAttributes.addFlashAttribute(OrderConstants.ERROR, OrderConstants.MSG_ORDER_NOT_FOUND);
                return OrderConstants.REDIRECT_ADMIN_ORDERS;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(OrderConstants.ERROR, OrderConstants.MSG_ERROR_LOADING_ORDER_DETAILS + e.getMessage());
            return OrderConstants.REDIRECT_ADMIN_ORDERS;
        }
    }

    /**
     * Update order status with validation
     */
    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(@PathVariable String orderId, 
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order != null) {
                // Validate status transition
                if (isValidStatusTransition(order.getStatus(), status)) {
                    order.setStatus(status);
                    orderService.updateOrder(order);
                    redirectAttributes.addFlashAttribute(OrderConstants.SUCCESS, OrderConstants.MSG_ORDER_STATUS_UPDATED + status);
                } else {
                    redirectAttributes.addFlashAttribute(OrderConstants.ERROR, 
                        OrderConstants.MSG_INVALID_STATUS_TRANSITION + order.getStatus() + " to " + status);
                }
            } else {
                redirectAttributes.addFlashAttribute(OrderConstants.ERROR, OrderConstants.MSG_ORDER_NOT_FOUND);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(OrderConstants.ERROR, OrderConstants.MSG_ERROR_UPDATING_ORDER_STATUS + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    // =========================== ORDER STATISTICS ===========================

    /**
     * View order statistics and analytics
     */
    @GetMapping("/statistics")
    public String viewStatistics(Model model) {
        try {
            // Get order statistics
            List<Order> allOrders = orderService.getAllOrders();
            
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream()
                .filter(o -> OrderConstants.STATUS_PENDING.equals(o.getStatus())).count();
            long confirmedOrders = allOrders.stream()
                .filter(o -> OrderConstants.STATUS_CONFIRMED.equals(o.getStatus())).count();
            long shippedOrders = allOrders.stream()
                .filter(o -> OrderConstants.STATUS_SHIPPED.equals(o.getStatus())).count();
            long deliveredOrders = allOrders.stream()
                .filter(o -> OrderConstants.STATUS_DELIVERED.equals(o.getStatus())).count();
            long cancelledOrders = allOrders.stream()
                .filter(o -> OrderConstants.STATUS_CANCELLED.equals(o.getStatus())).count();
            
            double totalRevenue = allOrders.stream()
                    .filter(o -> !OrderConstants.STATUS_CANCELLED.equals(o.getStatus()))
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            
            model.addAttribute(OrderConstants.TOTAL_ORDERS, totalOrders);
            model.addAttribute(OrderConstants.PENDING_ORDERS, pendingOrders);
            model.addAttribute(OrderConstants.CONFIRMED_ORDERS, confirmedOrders);
            model.addAttribute(OrderConstants.SHIPPED_ORDERS, shippedOrders);
            model.addAttribute(OrderConstants.DELIVERED_ORDERS, deliveredOrders);
            model.addAttribute(OrderConstants.CANCELLED_ORDERS, cancelledOrders);
            model.addAttribute(OrderConstants.TOTAL_REVENUE, totalRevenue);
            
            return "admin/orders/statistics";
        } catch (Exception e) {
            model.addAttribute(OrderConstants.ERROR, OrderConstants.MSG_ERROR_LOADING_STATISTICS + e.getMessage());
            return "admin/orders/list";
        }
    }
} 