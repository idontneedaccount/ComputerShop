package com.example.computershop.service;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order, List<OrderDetail> orderDetails);
    Order getOrderById(String id);
    List<Order> getAllOrders();
} 