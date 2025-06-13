package com.example.computershop.repository;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    List<Cart> findByUser(User user);
    List<Cart> findByUserUserId(String userId);
} 