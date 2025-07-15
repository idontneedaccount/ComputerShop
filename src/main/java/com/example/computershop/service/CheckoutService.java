package com.example.computershop.service;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;

import java.util.List;
import java.util.Map;

/**
 * Service interface for checkout operations
 */
public interface CheckoutService {
    
    /**
     * Process complete checkout flow
     */
    Order processCheckout(CheckoutRequest request, User user, List<Cart> cartItems);
    
    /**
     * Create Order entity from checkout request with voucher support
     */
    Order createOrderWithVoucher(CheckoutRequest request, List<Cart> cartItems);
    
    /**
     * Create OrderDetail entities from cart items
     */
    List<OrderDetail> createOrderDetails(List<Cart> cartItems, Order order);
    
    /**
     * Calculate cart totals (original amount, discount, final total)
     */
    Map<String, Long> calculateCheckoutTotals(List<Cart> cartItems);
    
    /**
     * Calculate original total before discounts
     */
    Long calculateCartOriginalTotal(List<Cart> cartItems);
    
    /**
     * Validate checkout data before processing
     */
    void validateCheckoutData(CheckoutRequest request, User user, List<Cart> cartItems);
    
    /**
     * Clear user cart after successful checkout
     */
    void clearUserCart(User user);
} 