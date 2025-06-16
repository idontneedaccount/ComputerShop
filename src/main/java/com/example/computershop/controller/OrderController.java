package com.example.computershop.controller;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.Cart;
import com.example.computershop.service.OrderService;
import com.example.computershop.repository.ProductRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {
    private final OrderService orderService;
    private final ProductRepository productRepository;

    public OrderController(OrderService orderService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String listAllOrders(@RequestParam(required = false) String status, Model model) {
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            orders = orderService.getAllOrders();
        }
        model.addAttribute("orders", orders);
        
        // Add status options for filtering
        List<String> statusOptions = List.of("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        model.addAttribute("statusOptions", statusOptions);
        
        return "admin/orders/list";
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable String orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order != null) {
                model.addAttribute("order", order);
                
                // Add available status transitions
                List<String> availableStatuses = getAvailableStatusTransitions(order.getStatus());
                model.addAttribute("availableStatuses", availableStatuses);
                
                return "admin/orders/details";
            } else {
                redirectAttributes.addFlashAttribute("error", "Order not found!");
                return "redirect:/admin/orders";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading order details: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

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
                    redirectAttributes.addFlashAttribute("success", "Order status updated successfully to " + status);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Invalid status transition from " + order.getStatus() + " to " + status);
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Order not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating order status: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + orderId;
    }

    @GetMapping("/statistics")
    public String viewStatistics(Model model) {
        try {
            // Get order statistics
            List<Order> allOrders = orderService.getAllOrders();
            
            long totalOrders = allOrders.size();
            long pendingOrders = allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count();
            long confirmedOrders = allOrders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count();
            long shippedOrders = allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count();
            long deliveredOrders = allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count();
            long cancelledOrders = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();
            
            double totalRevenue = allOrders.stream()
                    .filter(o -> !"CANCELLED".equals(o.getStatus()))
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            
            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("pendingOrders", pendingOrders);
            model.addAttribute("confirmedOrders", confirmedOrders);
            model.addAttribute("shippedOrders", shippedOrders);
            model.addAttribute("deliveredOrders", deliveredOrders);
            model.addAttribute("cancelledOrders", cancelledOrders);
            model.addAttribute("totalRevenue", totalRevenue);
            
            return "admin/orders/statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading statistics: " + e.getMessage());
            return "admin/orders/list";
        }
    }

    private List<String> getAvailableStatusTransitions(String currentStatus) {
        List<String> transitions = new ArrayList<>();
        
        switch (currentStatus) {
            case "PENDING":
                transitions.add("CONFIRMED");
                transitions.add("CANCELLED");
                break;
            case "CONFIRMED":
                transitions.add("PROCESSING");
                transitions.add("CANCELLED");
                break;
            case "PROCESSING":
                transitions.add("SHIPPED");
                transitions.add("CANCELLED");
                break;
            case "SHIPPED":
                transitions.add("DELIVERED");
                break;
            case "DELIVERED":
                // No further transitions
                break;
            case "CANCELLED":
                // No further transitions
                break;
        }
        
        return transitions;
    }

    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        List<String> validTransitions = getAvailableStatusTransitions(fromStatus);
        return validTransitions.contains(toStatus);
    }
} 