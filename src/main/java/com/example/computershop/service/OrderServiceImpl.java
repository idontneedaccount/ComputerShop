package com.example.computershop.service;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;
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
        try {
            // Set order properties
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setOrderDetails(null); // Clear any existing details
            
            // Save order first
            Order savedOrder = orderRepository.save(order);
            
            // Save order details
            for (OrderDetail detail : orderDetails) {
                detail.setOrder(savedOrder);
                detail.setTotalPrice(detail.getUnitPrice() * detail.getQuantity());
                orderDetailRepository.save(detail);
            }
            
            // Set the details back for returning
            savedOrder.setOrderDetails(orderDetails);
            
            return savedOrder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrderByIdWithDetails(String id) {
        return orderRepository.findByIdWithDetails(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserWithDetails(User user) {
        return orderRepository.findByUserWithDetails(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    @Override
    @Transactional
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }
} 