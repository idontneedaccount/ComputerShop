package com.example.computershop.repository;

import com.example.computershop.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    @Query("SELECT u.userId, u.fullName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
            "FROM User u LEFT JOIN u.orders o " +
            "GROUP BY u.userId, u.fullName " +
            "ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC")
    List<Object[]> getUserByRevenueOrder(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u")
    long countUsers();
    
    @Override
    List<User> findAll();

    @Override
    Optional<User> findById(String userId);
}
