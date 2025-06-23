package com.example.computershop.dto;

import com.example.computershop.entity.Products;

/**
 * Display DTO for cart items in templates
 */
public class CartItemDisplay {
    private Products product;
    private Integer quantity;

    public CartItemDisplay(Products product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Products getProduct() { 
        return product; 
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
} 