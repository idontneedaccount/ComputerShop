package com.example.computershop.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.example.computershop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
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
