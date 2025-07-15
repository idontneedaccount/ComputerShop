package com.example.computershop.controller;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.User;
import com.example.computershop.entity.Role;
import com.example.computershop.service.CartService;
import com.example.computershop.service.CheckoutService;
import com.example.computershop.service.ProductVariantService;
import com.example.computershop.service.VoucherService;
import com.example.computershop.exception.CartConstants;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    // ===== CORE SERVICES (Refactored) =====
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final OrderService orderService;
    
    // ===== LEGACY DEPENDENCIES (Kept for /add-variant and other dev features) =====
    private final ProductRepository repo; // For legacy code compatibility  
    private final CartRepository cartRepository; // Used in /add-variant endpoint
    private final UserRepository userRepository; // Injected but delegated to CartService
    private final ProductVariantService productVariantService; // Used in /add-variant endpoint
    private final VoucherService voucherService; // Injected but delegated to CartService

    public CartController(ProductRepository repo, OrderService orderService, CartService cartService, CheckoutService checkoutService, CartRepository cartRepository, UserRepository userRepository, ProductVariantService productVariantService, VoucherService voucherService) {
        this.repo = repo;
        this.orderService = orderService;
        this.cartService = cartService;
        this.checkoutService = checkoutService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productVariantService = productVariantService;
        this.voucherService = voucherService;
    }

    // =========================== HELPER METHODS ===========================

    // ❌ REMOVED - All helper methods moved to CartService

    // ❌ REMOVED - validateProductAvailability moved to CartService

    // =========================== CART CRUD OPERATIONS ===========================

    /**
     * Add product to cart - ✅ REFACTORED to use CartService
     */
    @RequestMapping(value = "/add/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public Object add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      Principal principal, RedirectAttributes redirectAttributes,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        if (isAjax) {
            // For AJAX requests, use existing AJAX method
            return cartService.addToCart(id, null, sl, principal);
        } else {
            // For non-AJAX requests, use new non-AJAX method
            String result = cartService.addProductToCart(id, sl, principal);
            
            if (result.startsWith("error:")) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, result.substring(6));
                return CartConstants.REDIRECT_CART_VIEW;
            } else if (result.startsWith("redirect:")) {
                return result.substring(9);
            }
            
            return CartConstants.REDIRECT_CART_VIEW;
        }
    }

    // ❌ REMOVED - processCartAddition logic moved to CartService

    /**
     * View cart contents - ✅ REFACTORED to use CartService
     */
    @GetMapping("/view")
    public String view(Model model, Principal principal) {
        try {
            Map<String, Object> viewData = cartService.prepareCartViewData(principal);
            model.addAllAttributes(viewData);
            
            if (viewData.containsKey("error")) {
                return CartConstants.REDIRECT_ERROR;
            }
            
            return "Cart/cart";
        } catch (Exception e) {
            return CartConstants.REDIRECT_ERROR;
        }
    }

    /**
     * Update cart item quantity - ✅ REFACTORED to use CartService
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @RequestParam int sl,
                         Principal principal, RedirectAttributes redirectAttributes) {
        String result = cartService.updateCartQuantity(id, sl, principal);
        
        if (result.startsWith("error:")) {
            redirectAttributes.addFlashAttribute(CartConstants.ERROR, result.substring(6));
        }
        
        return CartConstants.REDIRECT_CART_VIEW;
    }

    /**
     * Remove item from cart - ✅ REFACTORED to use CartService
     */
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, Principal principal) {
        String result = cartService.removeCartItem(id, principal);
        
        if (result.startsWith("error:")) {
            return CartConstants.REDIRECT_ERROR;
        }
        
        return CartConstants.REDIRECT_CART_VIEW;
    }

    /**
     * Clear all items from cart - ✅ REFACTORED to use CartService
     */
    @GetMapping("/clear")
    public String clear(Principal principal) {
        String result = cartService.clearUserCart(principal);
        
        if (result.startsWith("error:")) {
            return CartConstants.REDIRECT_ERROR;
        }
        
        return CartConstants.REDIRECT_CART_VIEW;
    }

    // =========================== VOUCHER OPERATIONS ===========================

    /**
     * Apply voucher to cart - ✅ REFACTORED to use CartService
     */
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String voucherCode, 
                              @RequestParam(defaultValue = "cart") String redirect,
                              Principal principal, 
                              RedirectAttributes redirectAttributes) {
        String result = cartService.applyVoucherToCart(voucherCode, principal);
        
        if (result.startsWith("error:")) {
            if (result.contains("đăng nhập")) {
                redirectAttributes.addFlashAttribute("error", result.substring(6));
                return "redirect:/auth/login";
            } else if (result.contains("trống")) {
                redirectAttributes.addFlashAttribute("error", result.substring(6));
                return "redirect:/cart/view";
            } else {
                redirectAttributes.addFlashAttribute("voucherError", result.substring(6));
            }
        } else if (result.startsWith("success:")) {
            redirectAttributes.addFlashAttribute("voucherSuccess", result.substring(8));
        }
        
        return getRedirectPath(redirect);
    }

    /**
     * Remove voucher from cart - ✅ REFACTORED to use CartService
     */
    @GetMapping("/remove-voucher")
    public String removeVoucher(@RequestParam(defaultValue = "cart") String redirect,
                               Principal principal, 
                               RedirectAttributes redirectAttributes) {
        String result = cartService.removeVoucherFromCart(principal);
        
        if (result.startsWith("error:")) {
            if (result.contains("đăng nhập")) {
                return "redirect:/auth/login";
            } else {
                redirectAttributes.addFlashAttribute("voucherError", result.substring(6));
            }
        } else if (result.startsWith("success:")) {
            redirectAttributes.addFlashAttribute("voucherSuccess", result.substring(8));
        }
        
        return getRedirectPath(redirect);
    }

    // ❌ REMOVED - All voucher helper methods moved to CartService

    /**
     * Get redirect path based on redirect parameter
     */
    private String getRedirectPath(String redirect) {
        switch (redirect.toLowerCase()) {
            case "checkout":
                return "redirect:/cart/checkout";
            case "cart":
            default:
                return "redirect:/cart/view";
        }
    }

    /**
     * Check if user has admin role (supports both Admin and ADMIN for backward compatibility)
     */
    private boolean isAdminRole(Role role) {
        if (role == null) return false;
        String roleName = role.name();
        return "Admin".equals(roleName) || "ADMIN".equals(roleName);
    }

    // ❌ REMOVED - formatCurrency and safeGetUsageCount moved to CartService

    // =========================== CHECKOUT OPERATIONS ===========================

    /**
     * Display checkout page - ✅ REFACTORED to use CartService
     */
    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {
        try {
            Map<String, Object> checkoutData = cartService.prepareCheckoutData(principal);
            
            if ((Boolean) checkoutData.getOrDefault("isEmpty", false)) {
                return CartConstants.REDIRECT_CART_VIEW;
            }
            
            model.addAllAttributes(checkoutData);
            
            if (checkoutData.containsKey("error")) {
                return "error";
            }

            return "Cart/Checkout";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * Process checkout form submission
     */
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("address") String address,
                                  @RequestParam("city") String city,
                                  @RequestParam("region") String region,
                                  @RequestParam(value = "district", required = false) String district,
                                  @RequestParam(value = "ward", required = false) String ward,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam("paymentMethod") String paymentMethod,
                                  @RequestParam("shippingMethod") String shippingMethod,
                                  @RequestParam(value = "distance", required = false) Double distance,
                                  @RequestParam(value = "shippingFee", required = false) Long shippingFee,
                                  Model model, Principal principal) {

        CheckoutRequest request = new CheckoutRequest();
        // ❌ REMOVED - Các field này không còn tồn tại trong CheckoutRequest
        // request.setFullName(fullName);
        // request.setEmail(email);
        // request.setPhone(phone);
        // request.setAddress(address);
        
        // ✅ NEW - Set shipping address và alternative receiver nếu cần
        if (address != null && !address.trim().isEmpty()) {
            request.setShippingAddress(address);
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            request.setAlternativeReceiverName(fullName);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            request.setAlternativeReceiverPhone(phone);
        }
        request.setCity(city);
        request.setRegion(region);
        request.setDistrict(district);
        request.setWard(ward);
        request.setNote(note);
        request.setPaymentMethod(paymentMethod);
        request.setShippingMethod(shippingMethod);
        request.setDistance(distance);
        request.setShippingFee(shippingFee);

        return processCheckoutInternal(request, model, principal);
    }

    /**
     * Internal checkout processing logic - ✅ REFACTORED to use CheckoutService
     */
    private String processCheckoutInternal(CheckoutRequest request, Model model, Principal principal) {
        try {
            User user = cartService.getUserFromPrincipal(principal);
            if (user == null) {
                model.addAttribute(CartConstants.ERROR, CartConstants.MSG_LOGIN_TO_ORDER);
                return CartConstants.CART_CHECKOUT_VIEW;
            }

            List<Cart> userCart = cartService.getCurrentUserCart(principal);
            if (userCart == null || userCart.isEmpty()) {
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // ✅ IMPROVED: Use CheckoutService for all checkout logic
            Order savedOrder = checkoutService.processCheckout(request, user, userCart);

            model.addAttribute(CartConstants.ORDER, savedOrder);
            return "Cart/orderDetails";

        } catch (Exception e) {
            model.addAttribute(CartConstants.ERROR, "Error processing order: " + e.getMessage());
            return CartConstants.CART_CHECKOUT_VIEW;
        }
    }

    // ❌ REMOVED - createOrderWithVoucher, createOrder, createOrderDetails moved to CheckoutService

    // =========================== ORDER OPERATIONS ===========================

    /**
     * View specific order details
     */
    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable String orderId, Model model, Principal principal) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                model.addAttribute(CartConstants.ERROR, CartConstants.MSG_ORDER_NOT_FOUND);
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Check access permission - merged if conditions
            if (principal != null) {
                User user = cartService.getUserFromPrincipal(principal);
                if (user != null && order.getUserId() != null && 
                    !order.getUserId().equals(user.getUserId()) && 
                    !isAdminRole(user.getRole())) {
                    model.addAttribute(CartConstants.ERROR, CartConstants.MSG_PERMISSION_DENIED);
                    return CartConstants.REDIRECT_CART_VIEW;
                }
            }

            model.addAttribute(CartConstants.ORDER, order);
            return "Cart/orderDetails";
        } catch (Exception e) {
            model.addAttribute(CartConstants.ERROR, "Có lỗi xảy ra khi tải thông tin đơn hàng: " + e.getMessage());
            return CartConstants.REDIRECT_CART_VIEW;
        }
    }

    // =========================== API ENDPOINTS ===========================

    /**
     * Get cart count for current user - ✅ REFACTORED to use CartService
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(Principal principal) {
        return cartService.getCartCount(principal);
    }

    /**
     * Get cart review content for AJAX updates - ✅ REFACTORED to use CartService
     */
    @GetMapping("/review")
    public String getCartReview(Model model, Principal principal) {
        try {
            Map<String, Object> reviewData = cartService.prepareCartReviewData(principal);
            model.addAllAttributes(reviewData);

            return "user/fragments/cartreview :: cartreview";
        } catch (Exception e) {
            return "user/fragments/cartreview :: cartreview";
        }
    }



    /**
     * Add product variant to cart - New feature from PR #23
     */
    @PostMapping("/add-variant")
    @ResponseBody
    public ResponseEntity<?> addVariant(@RequestParam String productId,
                                       @RequestParam String variantId,
                                       @RequestParam(defaultValue = "1") int quantity,
                                       Principal principal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (quantity <= 0) {
                response.put("success", false);
                response.put("message", "Số lượng phải lớn hơn 0!");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra đăng nhập
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
                return ResponseEntity.status(401).body(response);
            }
            
            // Lấy thông tin user
            User user = cartService.getUserFromPrincipal(principal);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng!");
                return ResponseEntity.status(401).body(response);
            }
            
            // Lấy thông tin variant
            ProductVariant variant = productVariantService.findById(variantId);
            if (variant == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy cấu hình sản phẩm!");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra variant còn hoạt động
            if (variant.getIsActive() == null || !variant.getIsActive()) {
                response.put("success", false);
                response.put("message", "Cấu hình sản phẩm này hiện không còn bán!");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Kiểm tra tồn kho - ✅ IMPROVED: Use CartService for better abstraction
            List<Cart> userCart = cartService.getCurrentUserCart(principal);
            int currentQuantity = 0;
            Cart existingCartItem = null;
            
            for (Cart c : userCart) {
                if (c.getVariant() != null && c.getVariant().getVariantId().equals(variantId)) {
                    currentQuantity = c.getQuantity();
                    existingCartItem = c;
                    break;
                }
            }
            
            int totalRequestedQuantity = currentQuantity + quantity;
            if (totalRequestedQuantity > variant.getQuantity()) {
                String errorMsg = String.format("Không đủ hàng trong kho! Hiện tại chỉ còn %d sản phẩm, bạn đã có %d trong giỏ hàng.", 
                    variant.getQuantity(), currentQuantity);
                response.put("success", false);
                response.put("message", errorMsg);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Cập nhật hoặc thêm mới vào giỏ hàng
            if (existingCartItem != null) {
                existingCartItem.setQuantity(totalRequestedQuantity);
                cartRepository.save(existingCartItem);
            } else {
                Cart newItem = new Cart();
                newItem.setProduct(variant.getProduct());
                newItem.setVariant(variant);
                newItem.setQuantity(quantity);
                newItem.setUser(user);
                newItem.setCreatedAt(LocalDateTime.now());
                cartRepository.save(newItem);
            }
            
            // Tính lại cart count
            int cartCount = cartRepository.findByUser(user).stream()
                .mapToInt(Cart::getQuantity)
                .sum();
            
            response.put("success", true);
            response.put("message", String.format("Đã thêm %s vào giỏ hàng!", variant.getDisplayName()));
            response.put("cartCount", cartCount);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
