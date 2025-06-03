package com.example.computershop.service;

import com.example.computershop.dto.ProductSalesDTO;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public List<Products> findTop5ProductsByStock() {
        return repo.findTop5ProductsByStock(PageRequest.of(0,5));
    }

    @Override
    public List<ProductSalesDTO> findTop5BestSellingProducts() {
        return repo.findTop5BestSellingProducts(PageRequest.of(0,5));
    }

    @Override
    public long countProducts() {
        return repo.countProducts();
    }


}
