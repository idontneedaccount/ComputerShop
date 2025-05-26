package com.example.computershop.service;

import com.example.computershop.entity.Product;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ProductServiceImpl implements ProductService{
    @Autowired
    private ProductRepository repo;
    @Override
    public List<Product> getAll() {
        return this.repo.findAll();
    }

    @Override
    public Boolean create(Product product) {
            try {
                this.repo.save(product);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        return false;
    }

    @Override
    public Product findById(String productID) {
        return this.repo.findById(productID).get();
    }

    @Override
    public Boolean update(Product product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Placeholder return statement
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
}
