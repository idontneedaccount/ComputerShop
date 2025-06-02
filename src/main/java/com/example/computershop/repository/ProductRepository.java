package com.example.computershop.repository;

import com.example.computershop.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Products, String> {

}