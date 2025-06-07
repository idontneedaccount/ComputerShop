package com.example.computershop.service;

import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.repository.ProductSpecificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductSpecificationService {

    private ProductSpecificationRepository specificationRepository;
    
    public ProductSpecifications findByProductId(String productId) {
        return specificationRepository.findById(productId).orElse(null);
    }
    
    public ProductSpecifications save(ProductSpecifications specification) {
        return specificationRepository.save(specification);
    }
} 