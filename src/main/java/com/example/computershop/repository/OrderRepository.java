package com.example.computershop.repository;

import com.example.computershop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Override
    Optional<Order> findById(String orderId);

} 