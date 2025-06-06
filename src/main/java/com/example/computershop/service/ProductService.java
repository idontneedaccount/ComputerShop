package com.example.computershop.service;

import java.util.List;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    public List<Products> getAll() {
        return this.repo.findAll();
    }

    public Boolean create(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Products findById(String productID) {
        return this.repo.findById(productID).orElse(null);
    }

    public Boolean update(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean delete(String productID) {
        try {
            this.repo.deleteById(productID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }
}
