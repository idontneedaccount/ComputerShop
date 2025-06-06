package com.example.computershop.service;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.repository.OrderRepository;
import com.example.computershop.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderDetail> orderDetails) {
        System.out.println("=== ORDER SERVICE: CREATE ORDER START ===");
        try {
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setOrderDetails(null);
            
            System.out.println("Saving order to database...");
            System.out.println("Order details: " + order.getFullName() + ", " + order.getEmail() + ", Total: " + order.getTotalAmount());
            
            Order savedOrder = orderRepository.save(order);
            System.out.println("Order saved with ID: " + savedOrder.getId());
            
            for (OrderDetail detail : orderDetails) {
                detail.setOrder(savedOrder);
                detail.setTotalPrice(detail.getUnitPrice() * detail.getQuantity());
                
                System.out.println("Saving order detail: Product=" + detail.getProduct().getName() + 
                    ", Qty=" + detail.getQuantity() + ", Price=" + detail.getUnitPrice());
                
                OrderDetail savedDetail = orderDetailRepository.save(detail);
                System.out.println("Order detail saved with ID: " + savedDetail.getOrderDetailID());
            }
            
            System.out.println("=== ORDER SERVICE: CREATE ORDER COMPLETE ===");
            return savedOrder;
        } catch (Exception e) {
            System.out.println("ERROR in OrderService.createOrder: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to trigger rollback
        }
    }

    @Override
    public Order getOrderById(Long id) {
        System.out.println("=== ORDER SERVICE: GET ORDER BY ID " + id + " ===");
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order != null) {
                System.out.println("Order found: " + order.getFullName());
                // Manually load order details to avoid lazy loading issues
                int detailsCount = order.getOrderDetails().size(); // Trigger lazy loading
                System.out.println("Order details loaded: " + detailsCount + " items");
            } else {
                System.out.println("Order not found with ID: " + id);
            }
            return order;
        } catch (Exception e) {
            System.out.println("ERROR in OrderService.getOrderById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
} 