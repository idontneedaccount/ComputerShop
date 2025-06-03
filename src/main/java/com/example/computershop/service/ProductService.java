package com.example.computershop.service;

import java.util.List;

import com.example.computershop.entity.Products;

public interface ProductService {
    List<Products> getAll();
    Boolean create(Products product);
    Products findById(String productID);
    Boolean update(Products product);
    Boolean delete(String productID);
    boolean existsByName(String name);
}
