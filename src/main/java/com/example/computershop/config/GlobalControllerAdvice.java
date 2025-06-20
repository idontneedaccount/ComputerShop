package com.example.computershop.config;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    // Display class for cart items
    public static class CartItemDisplay {
        private Products product;
        private Integer quantity;
        
        public CartItemDisplay(Products product, Integer quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public Products getProduct() { return product; }
        public Integer getQuantity() { return quantity; }
    }
    
    @ModelAttribute("cartCount")
    public int cartCount(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<Cart> userCart = cartRepository.findByUser(user);
                return userCart.stream().mapToInt(Cart::getQuantity).sum();
            }
        }
        return 0;
    }
    
    @ModelAttribute("cartItems")
    public List<CartItemDisplay> cartItems(Principal principal) {
        List<CartItemDisplay> displayItems = new ArrayList<>();
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<Cart> userCart = cartRepository.findByUser(user);
                for (Cart item : userCart) {
                    Products product = productRepository.findById(item.getProduct().getProductID()).orElse(null);
                    if (product != null) {
                        displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                    }
                }
            }
        }
        return displayItems;
    }
    
    @ModelAttribute("cartTotal")
    public long cartTotal(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<Cart> userCart = cartRepository.findByUser(user);
                long total = 0;
                for (Cart item : userCart) {
                    Products product = productRepository.findById(item.getProduct().getProductID()).orElse(null);
                    if (product != null) {
                        total += product.getPrice().longValue() * item.getQuantity();
                    }
                }
                return total;
            }
        }
        return 0;
    }
} 