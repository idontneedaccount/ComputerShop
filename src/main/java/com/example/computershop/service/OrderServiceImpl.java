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
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderDetail> orderDetails) {
        try {
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setOrderDetails(null);
            
            Order savedOrder = orderRepository.save(order);

            for (OrderDetail detail : orderDetails) {
                detail.setOrder(savedOrder);
                detail.setTotalPrice(detail.getUnitPrice() * detail.getQuantity());
                OrderDetail savedDetail = orderDetailRepository.save(detail);
            }
            return savedOrder;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Order getOrderById(Long id) {
        try {
            Order order = orderRepository.findById(id).orElse(null);
            if (order != null) {
                int detailsCount = order.getOrderDetails().size();
            }
            return order;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
} 