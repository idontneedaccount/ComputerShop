package com.example.computershop.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private String productId;
    private Integer quantity;
    
    // Constructor for backward compatibility
    public CartItem(Product product, Integer quantity) {
        this.productId = product.getProductID();
        this.quantity = quantity;
    }
    
    // Helper method to get product ID
    public String getProductId() {
        return productId;
    }
}
