package com.example.computershop.service;

import java.util.List;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String ERROR = "Error creating category: {}";
    
    public List<Products> getAll() {
        return this.repo.findAll();
    }
    
    // Optimized method for homepage - no specifications loaded
    @Cacheable(value = "homepageProducts")
    public List<Products> getAllActiveForHomepage() {
        return this.repo.findAllActiveForHomepage();
    }
    
    // Optimized method when specifications are needed
    @Cacheable(value = "activeProducts")
    public List<Products> getAllActiveWithSpecifications() {
        return this.repo.findAllActiveWithSpecifications();
    }
    
    // Optimized method for admin - all products with specifications
    @Cacheable(value = "products")
    public List<Products> getAllWithSpecifications() {
        return this.repo.findAllWithSpecifications();
    }

    @CacheEvict(value = {"products", "activeProducts", "homepageProducts", "brands"}, allEntries = true)
    public Boolean create(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            logger.error(ERROR, e.getMessage(), e);
        }
        return false;
    }

    public Products findById(String productID) {
        return this.repo.findById(productID).orElse(null);
    }

    @CacheEvict(value = {"products", "activeProducts", "homepageProducts", "brands"}, allEntries = true)
    public Boolean delete(String productID) {
        try {
            this.repo.deleteById(productID);
            return true;
        } catch (Exception e) {
            logger.error(ERROR, e.getMessage(), e);
        }
        return false;
    }

    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }

    @Cacheable(value = "brands")
    public List<String> getDistinctBrands() {
        return repo.findDistinctBrands();
    }
}
