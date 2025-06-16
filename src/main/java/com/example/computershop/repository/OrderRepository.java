package com.example.computershop.repository;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Override
    Optional<Order> findById(String orderId);
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") String orderId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findByUserWithDetails(@Param("user") User user);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatusWithDetails(@Param("status") String status);
} 