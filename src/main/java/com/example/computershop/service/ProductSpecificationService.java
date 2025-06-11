package com.example.computershop.service;

import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.repository.ProductSpecificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    
    public List<String> getDistinctCpus() {
        return specificationRepository.findDistinctCpus();
    }
    
    public List<String> getDistinctRams() {
        return specificationRepository.findDistinctRams();
    }
    
    public List<String> getDistinctSsds() {
        return specificationRepository.findDistinctSsds();
    }
    
    public List<String> getDistinctVgas() {
        return specificationRepository.findDistinctVgas();
    }
    
    public List<String> getDistinctScreens() {
        return specificationRepository.findDistinctScreens();
    }
} 