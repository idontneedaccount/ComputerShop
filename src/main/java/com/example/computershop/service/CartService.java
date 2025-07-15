package com.example.computershop.service;

import com.example.computershop.dto.CartItemDisplay;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Service interface for cart operations
 */
public interface CartService {
    
    /**
     * Get current user's cart items
     */
    List<Cart> getCurrentUserCart(Principal principal);
    
    /**
     * Get current user from principal (handles OAuth2 and form auth)
     */
    User getUserFromPrincipal(Principal principal);
    
    /**
     * Convert cart items to display DTOs
     */
    List<CartItemDisplay> convertToDisplayItems(List<Cart> cartItems);
    
    /**
     * Add product to cart
     */
    ResponseEntity<Map<String, Object>> addToCart(String productId, String variantId, Integer quantity, Principal principal);
    
    /**
     * Update cart item quantity
     */
    ResponseEntity<Map<String, Object>> updateCartItem(String productId, String variantId, Integer quantity, Principal principal);
    
    /**
     * Remove item from cart
     */
    ResponseEntity<Map<String, Object>> removeFromCart(String productId, String variantId, Principal principal);
    
    /**
     * Get cart count for current user
     */
    ResponseEntity<Map<String, Object>> getCartCount(Principal principal);
    
    /**
     * Apply voucher to cart
     */
    ResponseEntity<Map<String, Object>> applyVoucher(String voucherCode, Principal principal);
    
    /**
     * Remove voucher from cart
     */
    ResponseEntity<Map<String, Object>> removeVoucher(Principal principal);
    
    /**
     * Calculate cart totals (subtotal, discount, final total)
     */
    Map<String, Long> calculateCartTotals(List<Cart> cartItems);
    
    /**
     * Clear cart for user
     */
    void clearCart(User user);
    
    /**
     * Create error response for AJAX requests
     */
    ResponseEntity<Map<String, Object>> createErrorResponse(String message, int status);
    
    /**
     * Create success response for AJAX requests
     */
    ResponseEntity<Map<String, Object>> createSuccessResponse(Map<String, Object> data);
    
    // ===== NON-AJAX CART OPERATIONS =====
    
    /**
     * Add product to cart (non-AJAX version)
     */
    String addProductToCart(String productId, int quantity, Principal principal);
    
    /**
     * Update cart item quantity (non-AJAX version)
     */
    String updateCartQuantity(String productId, int quantity, Principal principal);
    
    /**
     * Remove item from cart (non-AJAX version)
     */
    String removeCartItem(String productId, Principal principal);
    
    /**
     * Clear user cart (non-AJAX version)
     */
    String clearUserCart(Principal principal);
    
    /**
     * Validate product availability
     */
    String validateProductAvailability(String productId);
    
    /**
     * Process cart addition with stock validation
     */
    Object processCartAddition(User user, String productId, int quantity, boolean isAjax);
    
    // ===== VOUCHER OPERATIONS =====
    
    /**
     * Apply voucher to cart (non-AJAX version)
     */
    String applyVoucherToCart(String voucherCode, Principal principal);
    
    /**
     * Remove voucher from cart (non-AJAX version)
     */
    String removeVoucherFromCart(Principal principal);
    
    /**
     * Calculate cart original total amount
     */
    Long calculateCartOriginalTotal(List<Cart> cartItems);
    
    /**
     * Distribute discount proportionally across cart items
     */
    void distributeDiscountToCartItems(List<Cart> cartItems, Long totalDiscount, String voucherCode);
    
    /**
     * Remove voucher from all cart items
     */
    void removeVoucherFromAllCartItems(List<Cart> cartItems);
    
    // ===== CART DISPLAY OPERATIONS =====
    
    /**
     * Prepare cart view data including totals and voucher info
     */
    Map<String, Object> prepareCartViewData(Principal principal);
    
    /**
     * Prepare checkout view data including totals and voucher info
     */
    Map<String, Object> prepareCheckoutData(Principal principal);
    
    /**
     * Prepare cart review data for AJAX updates
     */
    Map<String, Object> prepareCartReviewData(Principal principal);
} 