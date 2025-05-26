package com.example.computershop.service;

import com.example.computershop.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAll();
    Boolean create(Product product);
    Product findById(String productID);
    Boolean update(Product product);
    Boolean delete(String productID);
}
