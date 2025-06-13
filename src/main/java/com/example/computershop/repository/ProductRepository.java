package com.example.computershop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.computershop.entity.Products;

public interface ProductRepository extends JpaRepository<Products, String> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT p.brand FROM Products p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
}