package com.example.computershop.service;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.computershop.dto.ProductRatingDTO;
import com.example.computershop.dto.ProductSalesDTO;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository repo;
    private final ReviewService reviewService;
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
    public Boolean update(Products product) {
        try {
            this.repo.save(product);
            return true;
        } catch (Exception e) {
            logger.error(ERROR, e.getMessage(), e);
        }
        return false;
    }
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
    public List<Products> findTop5ProductsByStock() {
        return repo.findTop5ProductsByStock(PageRequest.of(0,5));
    }
    public List<ProductSalesDTO> findTop5BestSellingProducts() {
        return repo.findTop5BestSellingProducts(PageRequest.of(0,5));
    }
    public long countProducts() {
        return repo.countProducts();
    }
    
    /**
     * Lấy thông tin rating của sản phẩm
     */
    public ProductRatingDTO getProductRating(String productId) {
        return reviewService.getProductRating(productId);
    }
    
    /**
     * Lấy sản phẩm kèm thông tin rating
     */
    public Products findByIdWithRating(String productID) {
        Products product = findById(productID);
        if (product != null) {
            // Rating sẽ được lấy riêng trong controller để tránh N+1 problem
        }
        return product;
    }

}
