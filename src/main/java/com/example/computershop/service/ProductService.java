package com.example.computershop.service;

import com.example.computershop.entity.Products;

import java.util.List;

public interface ProductService {
    List<Products> getAll();
    Boolean create(Products product);
    Products findById(String productID);
    Boolean update(Products product);
    Boolean delete(String productID);
}
