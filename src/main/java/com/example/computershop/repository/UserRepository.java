package com.example.computershop.repository;

import com.example.computershop.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    @Query("SELECT u.userId, u.fullName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
            "FROM User u LEFT JOIN u.orders o " +
            "GROUP BY u.userId, u.fullName " +
            "ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC")
    List<Object[]> getUserByRevenueOrder(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u")
    long countUsers();
}
