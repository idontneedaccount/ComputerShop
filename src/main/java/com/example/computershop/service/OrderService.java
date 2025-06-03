package com.example.computershop.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

public interface OrderService {
    List<Object[]> getMonthlyRevenue(int year);
    List<Integer> getDistinctYears();
    long countOrders();
    BigInteger getTotalRevenue();
}
