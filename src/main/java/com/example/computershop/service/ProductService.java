package com.example.computershop.service;

import com.example.computershop.dto.ProductSalesDTO;
import com.example.computershop.entity.Products;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<Products> getAll();
    Boolean create(Products product);
    Products findById(String productID);
    Boolean update(Products product);
    Boolean delete(String productID);
    List<Products> findTop5ProductsByStock();
    List<ProductSalesDTO> findTop5BestSellingProducts();
    long countProducts();
}
