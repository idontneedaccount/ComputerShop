package com.example.computershop.config;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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

    /**
     * Get user from principal - handles both OAuth2 and form authentication
     */
    private User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        // Handle OAuth2 authentication
        if (principal instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String email = getEmailFromOAuth2Attributes(provider, oauth2User.getAttributes());
            
            if (email != null) {
                // OAuth2 users are stored with username = email
                return userRepository.findByUsername(email).orElse(null);
            }
            return null;
        }

        // Handle form authentication
        String identifier = principal.getName();
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);
    }

    /**
     * Extract email from OAuth2 attributes based on provider
     */
    private String getEmailFromOAuth2Attributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("email");
            } else if ("github".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String username = (String) attributes.get("login");
                    if (username != null) {
                        email = username + "@github.com";
                    }
                }
                return email;
            } else if ("facebook".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String facebookId = (String) attributes.get("id");
                    if (facebookId != null) {
                        email = "facebook_" + facebookId + "@facebook.com";
                    }
                }
                return email;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @ModelAttribute("cartCount")
    public int cartCount(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            List<Cart> userCart = cartRepository.findByUser(user);
            return userCart.stream().mapToInt(Cart::getQuantity).sum();
        }
        return 0;
    }
    
    @ModelAttribute("cartItems")
    public List<CartItemDisplay> cartItems(Principal principal) {
        List<CartItemDisplay> displayItems = new ArrayList<>();
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            List<Cart> userCart = cartRepository.findByUser(user);
            for (Cart item : userCart) {
                Products product = productRepository.findById(item.getProduct().getProductID()).orElse(null);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                }
            }
        }
        return displayItems;
    }
    
    @ModelAttribute("cartTotal")
    public long cartTotal(Principal principal) {
        User user = getUserFromPrincipal(principal);
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
        return 0;
    }
} 