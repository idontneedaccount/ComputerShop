package com.example.computershop.repository;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import java.math.BigInteger;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Override
    Optional<Order> findById(String orderId);
    
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") String orderId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserWithDetails(@Param("userId") String userId);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatusWithDetails(@Param("status") String status);
    @Query("SELECT MONTH(o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o WHERE YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate) ORDER BY MONTH(o.orderDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT DISTINCT YEAR(o.orderDate) FROM Order o ORDER BY YEAR(o.orderDate) DESC")
    List<Integer> getDistinctYears();

    @Query("SELECT COUNT(o) FROM Order o")
    long countOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigInteger getTotalRevenue();
} 
