package com.example.computershop.service;

import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@AllArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {
    private ProductRepository repo;

    @Override
    public List<Products> getAll() {
        return this.repo.findAll();
    }

    @Override
    public Boolean create(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Products findById(String productID) {
        return this.repo.findById(productID).orElse(null);
    }

    @Override
    public Boolean update(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean delete(String productID) {
        try {
            this.repo.deleteById(productID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }
}
