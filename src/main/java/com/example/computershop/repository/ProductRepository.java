package com.example.computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.computershop.entity.Products;

public interface ProductRepository extends JpaRepository<Products, String> {
    boolean existsByName(String name);
}