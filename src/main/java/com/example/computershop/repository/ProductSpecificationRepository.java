package com.example.computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.ProductSpecifications;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecifications, String> {
} 