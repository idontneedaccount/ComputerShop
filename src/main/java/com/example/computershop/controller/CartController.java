package com.example.computershop.controller;

import com.example.computershop.dto.CartItemDisplay;
import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;
import com.example.computershop.service.ProductVariantService;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    // Dependencies
    private final ProductRepository repo;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductVariantService productVariantService;

    public CartController(ProductRepository repo, OrderService orderService, CartRepository cartRepository, UserRepository userRepository, ProductVariantService productVariantService) {
        this.repo = repo;
        this.orderService = orderService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productVariantService = productVariantService;
    }

    // =========================== HELPER METHODS ===========================

    /**
     * Get current user's cart items
     */
    private List<Cart> getCurrentUserCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            return cartRepository.findByUser(user);
        }
        return new ArrayList<>();
    }

    /**
     * Load product for CartItem
     */
    private Products loadProduct(Cart cartItem) {
        return repo.findById(cartItem.getProduct().getProductID()).orElse(null);
    }

    // Display class for template with ProductVariant support
    public static class CartItemDisplay {
        private Products product;
        private ProductVariant variant;
        private Integer quantity;
        
        public CartItemDisplay(Products product, Integer quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public CartItemDisplay(Products product, ProductVariant variant, Integer quantity) {
            this.product = product;
            this.variant = variant;
            this.quantity = quantity;
        }
        
        public Products getProduct() { return product; }
        public ProductVariant getVariant() { return variant; }
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

    /**
     * Create error response for AJAX requests
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put(CartConstants.SUCCESS, false);
        response.put(CartConstants.MESSAGE, message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validate product availability
     */
    private String validateProductAvailability(Products product) {
        if (product == null) {
            return CartConstants.MSG_PRODUCT_NOT_FOUND;
        }
        if (product.getIsActive() == null || !product.getIsActive()) {
            return CartConstants.MSG_PRODUCT_NOT_AVAILABLE;
        }
        return null;
    }

    // =========================== CART CRUD OPERATIONS ===========================

    /**
     * Add product to cart
     */
    @RequestMapping(value = "/add/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public Object add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      Principal principal, RedirectAttributes redirectAttributes,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

        boolean isAjax = "XMLHttpRequest".equals(requestedWith);

        try {
            // Validate quantity
            if (sl <= 0) {
                if (isAjax) {
                    return createErrorResponse(CartConstants.MSG_QUANTITY_MUST_GREATER_ZERO, 400);
                }
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_QUANTITY_MUST_GREATER_ZERO);
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Validate product
            Products sp = repo.findById(id).orElse(null);
            String productError = validateProductAvailability(sp);
            if (productError != null) {
                if (isAjax) {
                    return createErrorResponse(productError, 400);
                }
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, productError);
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Validate user
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                if (isAjax) {
                    return createErrorResponse(CartConstants.MSG_LOGIN_TO_ADD_CART, 401);
                }
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_LOGIN_TO_ADD_CART);
                return CartConstants.REDIRECT_AUTH_LOGIN;
            }

            // Process cart addition
            return processCartAddition(user, sp, id, sl, isAjax);

        } catch (Exception e) {
            if (isAjax) {
                return createErrorResponse("Có lỗi xảy ra: " + e.getMessage(), 500);
            }
            redirectAttributes.addFlashAttribute(CartConstants.ERROR, "Có lỗi xảy ra khi thêm vào giỏ hàng: " + e.getMessage());
            return CartConstants.REDIRECT_CART_VIEW;
        }
    }

    /**
     * Process cart addition logic
     */
    private Object processCartAddition(User user, Products sp, String productId, int quantity, boolean isAjax) {
        List<Cart> userCart = cartRepository.findByUser(user);

        // Check existing cart item
        int currentCartQuantity = 0;
        Cart existingCartItem = null;
        for (Cart c : userCart) {
            if (c.getProduct().getProductID().equals(productId)) {
                currentCartQuantity = c.getQuantity();
                existingCartItem = c;
                break;
            }
        }

        // Check stock
        int totalRequestedQuantity = currentCartQuantity + quantity;
        if (totalRequestedQuantity > sp.getQuantity()) {
            String errorMsg = String.format("Không đủ hàng trong kho! Hiện tại chỉ còn %d sản phẩm, bạn đã có %d trong giỏ hàng.",
                    sp.getQuantity(), currentCartQuantity);
            if (isAjax) {
                return createErrorResponse(errorMsg, 400);
            }
            return errorMsg;
        }

        // Update or add cart item
        if (existingCartItem != null) {
            existingCartItem.setQuantity(totalRequestedQuantity);
            cartRepository.save(existingCartItem);
        } else {
            Cart newItem = new Cart();
            newItem.setProduct(sp);
            newItem.setQuantity(quantity);
            newItem.setUser(user);
            newItem.setCreatedAt(LocalDateTime.now());
            cartRepository.save(newItem);
        }

        // Return response
        if (isAjax) {
            Map<String, Object> response = new HashMap<>();
            response.put(CartConstants.SUCCESS, true);
            response.put(CartConstants.MESSAGE, String.format("Đã thêm %s vào giỏ hàng!", sp.getName()));
            int cartCount = cartRepository.findByUser(user).stream()
                    .mapToInt(Cart::getQuantity)
                    .sum();
            response.put(CartConstants.CART_COUNT, cartCount);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }

        return CartConstants.REDIRECT_CART_VIEW;
    }

    /**
     * View cart contents
     */
    @GetMapping("/view")
    public String view(Model model, Principal principal) {
        try {
            List<Cart> userCart = getCurrentUserCart(principal);
            List<CartItemDisplay> displayItems = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Cart item : userCart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));

                    BigDecimal price = new BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
                }
            }

            model.addAttribute(CartConstants.CART_ITEMS, displayItems);
            model.addAttribute(CartConstants.TOTAL, total);

            return "Cart/cart";
        } catch (Exception e) {
            return CartConstants.REDIRECT_ERROR;
        }
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @RequestParam int sl,
                         Principal principal, RedirectAttributes redirectAttributes) {
        try {
            // Validate quantity
            if (sl <= 0) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_QUANTITY_MUST_GREATER_ZERO);
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Validate user
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_LOGIN_REQUIRED);
                return CartConstants.REDIRECT_AUTH_LOGIN;
            }

            // Validate product and stock
            Products product = repo.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_PRODUCT_NOT_FOUND);
                return CartConstants.REDIRECT_CART_VIEW;
            }

            if (sl > product.getQuantity()) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR,
                        String.format("Không đủ hàng trong kho! Chỉ còn %d sản phẩm.", product.getQuantity()));
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Update cart
            List<Cart> userCart = cartRepository.findByUser(user);
            boolean updated = false;
            for (Cart c : userCart) {
                if (c.getProduct().getProductID().equals(id)) {
                    c.setQuantity(sl);
                    cartRepository.save(c);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                redirectAttributes.addFlashAttribute(CartConstants.ERROR, CartConstants.MSG_PRODUCT_NOT_IN_CART);
            }

            return CartConstants.REDIRECT_CART_VIEW;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(CartConstants.ERROR, "Có lỗi xảy ra khi cập nhật giỏ hàng: " + e.getMessage());
            return CartConstants.REDIRECT_CART_VIEW;
        }
    }

    /**
     * Remove item from cart
     */
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return CartConstants.REDIRECT_ERROR;
            }

            List<Cart> userCart = cartRepository.findByUser(user);

            Cart itemToRemove = null;
            for (Cart c : userCart) {
                if (c.getProduct().getProductID().equals(id)) {
                    itemToRemove = c;
                    break;
                }
            }

            if (itemToRemove != null) {
                cartRepository.delete(itemToRemove);
            }

            return CartConstants.REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return CartConstants.REDIRECT_ERROR;
        }
    }

    /**
     * Clear all items from cart
     */
    @GetMapping("/clear")
    public String clear(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user != null) {
                List<Cart> userCartItems = cartRepository.findByUser(user);
                cartRepository.deleteAll(userCartItems);
            }
            return CartConstants.REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return CartConstants.REDIRECT_ERROR;
        }
    }

    // =========================== CHECKOUT OPERATIONS ===========================

    /**
     * Display checkout page
     */
    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {
        try {
            List<Cart> userCart = getCurrentUserCart(principal);

            if (userCart == null || userCart.isEmpty()) {
                return CartConstants.REDIRECT_CART_VIEW;
            }

            List<CartItemDisplay> displayItems = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Cart item : userCart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                    BigDecimal price = new BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
                }
            }

            model.addAttribute(CartConstants.CART_ITEMS, displayItems);
            model.addAttribute(CartConstants.ORDER, new Order());
            model.addAttribute(CartConstants.TOTAL, total);

            return CartConstants.CART_CHECKOUT_VIEW;
        } catch (Exception e) {
            model.addAttribute(CartConstants.ERROR, e.getMessage());
            return CartConstants.ERROR;
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
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam("paymentMethod") String paymentMethod,
                                  Model model, Principal principal) {

        CheckoutRequest request = new CheckoutRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPhone(phone);
        request.setAddress(address);
        request.setCity(city);
        request.setRegion(region);
        request.setNote(note);
        request.setPaymentMethod(paymentMethod);

        return processCheckoutInternal(request, model, principal);
    }

    /**
     * Internal checkout processing logic
     */
    private String processCheckoutInternal(CheckoutRequest request, Model model, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                model.addAttribute(CartConstants.ERROR, CartConstants.MSG_LOGIN_TO_ORDER);
                return CartConstants.CART_CHECKOUT_VIEW;
            }

            List<Cart> userCart = cartRepository.findByUser(user);
            if (userCart == null || userCart.isEmpty()) {
                return CartConstants.REDIRECT_CART_VIEW;
            }

            // Create and save order
            Order order = createOrder(request);
            List<OrderDetail> orderDetails = createOrderDetails(userCart, order);

            order.setUserId(user.getUserId());
            if (!orderDetails.isEmpty()) {
                order.setProductId(orderDetails.get(0).getProduct().getProductID());
            }

            Order savedOrder = orderService.createOrder(order, orderDetails);
            cartRepository.deleteAll(userCart);

            savedOrder.setOrderDetails(orderDetails);
            model.addAttribute(CartConstants.ORDER, savedOrder);
            return "Cart/orderDetails";

        } catch (Exception e) {
            model.addAttribute(CartConstants.ERROR, "Error processing order: " + e.getMessage());
            return CartConstants.CART_CHECKOUT_VIEW;
        }
    }

    /**
     * Create Order entity from checkout request
     */
    private Order createOrder(CheckoutRequest request) {
        Order order = new Order();
        order.setFullName(request.getFullName());
        order.setEmail(request.getEmail());
        order.setPhone(request.getPhone());
        order.setAddress(request.getAddress());
        order.setShippingAddress(request.getAddress() + ", " + request.getCity() + ", " + request.getRegion());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNote(request.getNote());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        return order;
    }

    /**
     * Create OrderDetail entities from cart items
     */
    private List<OrderDetail> createOrderDetails(List<Cart> userCart, Order order) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        long total = 0;

        for (Cart cartItem : userCart) {
            Products product = loadProduct(cartItem);
            if (product != null) {
                OrderDetail detail = new OrderDetail();
                detail.setProduct(product);
                detail.setQuantity(cartItem.getQuantity());

                Long price = product.getPrice().longValue();
                detail.setUnitPrice(price);

                orderDetails.add(detail);
                total += price * cartItem.getQuantity();
            }
        }

        order.setTotalAmount(total);
        return orderDetails;
    }

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
                User user = getUserFromPrincipal(principal);
                if (user != null && order.getUserId() != null && 
                    !order.getUserId().equals(user.getUserId()) && 
                    !user.getRole().name().equals("ADMIN")) {
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
     * Get cart count for current user
     */
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        int count = 0;
        if (principal != null) {
            User user = getUserFromPrincipal(principal);
            if (user != null) {
                List<Cart> userCart = cartRepository.findByUser(user);
                count = userCart.stream().mapToInt(Cart::getQuantity).sum();
            }
        }
        response.put("count", count);
        return response;
    }

    /**
     * Get cart review content for AJAX updates
     */
    @GetMapping("/review")
    public String getCartReview(Model model, Principal principal) {
        try {
            List<Cart> userCart = getCurrentUserCart(principal);
            List<CartItemDisplay> displayItems = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Cart item : userCart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));

                    BigDecimal price = new BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
                }
            }

            model.addAttribute(CartConstants.CART_ITEMS, displayItems);
            model.addAttribute(CartConstants.TOTAL, total);

            return "user/fragments/cartreview :: cartreview";
        } catch (Exception e) {
            return "user/fragments/cartreview :: cartreview";
        }
    }

    /**
     * Debug endpoint to show cart information
     */
    @GetMapping("/debug")
    @ResponseBody
    public String debugCart(Principal principal) {
        if (principal == null) {
            return "No user logged in";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "User not found: " + username;
        }

        List<Cart> dbCart = cartRepository.findByUser(user);

        StringBuilder result = new StringBuilder();
        result.append("User: ").append(username).append("\n");
        result.append("Database Cart size: ").append(dbCart.size()).append("\n\n");

        result.append("Database Cart items:\n");
        for (Cart item : dbCart) {
            result.append("- Product ID: ").append(item.getProduct() != null ? item.getProduct().getProductID() : "NULL")
                  .append(", Quantity: ").append(item.getQuantity()).append("\n");
        }

        return result.toString();
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
            User user = getUserFromPrincipal(principal);
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
            
            // Kiểm tra tồn kho
            List<Cart> userCart = cartRepository.findByUser(user);
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
