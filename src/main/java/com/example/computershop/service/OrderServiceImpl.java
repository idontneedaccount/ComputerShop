package com.example.computershop.service;

import com.example.computershop.repository.OrderRepository;
import com.example.computershop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService{
    private OrderRepository repo;
    @Override
    public List<Object[]> getMonthlyRevenue(int year) {
        return repo.getMonthlyRevenue(year);
    }

    @Override
    public List<Integer> getDistinctYears() {
        return repo.getDistinctYears();
    }

    @Override
    public long countOrders() {
        return repo.countOrders();
    }

    @Override
    public BigInteger getTotalRevenue() {
        return repo.getTotalRevenue()  ;
    }

}
