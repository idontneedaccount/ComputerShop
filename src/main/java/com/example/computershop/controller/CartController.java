package com.example.computershop.controller;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.ProductVariantService;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
@Controller
@RequestMapping("/cart")
public class CartController {
    private static final String REDIRECT_ERROR = "redirect:/error";
    private static final String REDIRECT_CART_VIEW = "redirect:/cart/view";
    private static final String ORDER = "order";
    private static final String ERROR = "error";
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

    // Helper method to get current user's cart from database
    private List<Cart> getCurrentUserCart(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                return cartRepository.findByUser(user);
            }
        }
        return new ArrayList<>();
    }

    // Helper method to load product for CartItem
    private Products loadProduct(Cart cartItem) {
        return repo.findById(cartItem.getProduct().getProductID()).orElse(null);
    }

    // Display class for template
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

    @RequestMapping(value = "/add/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public Object add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      Principal principal,
                      RedirectAttributes redirectAttributes,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);
        
        try {
            // Validate input
            if (sl <= 0) {
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Số lượng phải lớn hơn 0!");
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("error", "Số lượng phải lớn hơn 0!");
                return REDIRECT_CART_VIEW;
            }
            
            Products sp = repo.findById(id).orElse(null);
            if(sp == null){
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Sản phẩm không tồn tại!");
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return REDIRECT_CART_VIEW;
            }
            
            // Kiểm tra sản phẩm còn hoạt động
            if (sp.getIsActive() == null || !sp.getIsActive()) {
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Sản phẩm này hiện không còn bán!");
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("error", "Sản phẩm này hiện không còn bán!");
                return REDIRECT_CART_VIEW;
            }
            
            // Lấy user hiện tại
            if (principal == null) {
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
                    return ResponseEntity.status(401).body(response);
                }
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng!");
                return "redirect:/auth/login";
            }
            String username = principal.getName();
            
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy thông tin người dùng!");
                    return ResponseEntity.status(401).body(response);
                }
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/auth/login";
            }
            
            // Get current user's cart from database
            List<Cart> userCart = cartRepository.findByUser(user);
            
            // Kiểm tra tồn kho
            int currentCartQuantity = 0;
            Cart existingCartItem = null;
            for(Cart c : userCart){
                if(c.getProduct().getProductID().equals(id)){
                    currentCartQuantity = c.getQuantity();
                    existingCartItem = c;
                    break;
                }
            }
            
            int totalRequestedQuantity = currentCartQuantity + sl;
            if (totalRequestedQuantity > sp.getQuantity()) {
                String errorMsg = String.format("Không đủ hàng trong kho! Hiện tại chỉ còn %d sản phẩm, bạn đã có %d trong giỏ hàng.", 
                    sp.getQuantity(), currentCartQuantity);
                if (isAjax) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", errorMsg);
                    return ResponseEntity.badRequest().body(response);
                }
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return REDIRECT_CART_VIEW;
            }
            
            // Cập nhật hoặc thêm mới vào giỏ hàng
            if (existingCartItem != null) {
                existingCartItem.setQuantity(totalRequestedQuantity);
                cartRepository.save(existingCartItem);
            } else {
                Cart newItem = new Cart();
                newItem.setProduct(sp);
                newItem.setQuantity(sl);
                newItem.setUser(user);
                newItem.setCreatedAt(LocalDateTime.now());
                cartRepository.save(newItem);
            }
            
            // Nếu là AJAX, trả về JSON với cart count
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", String.format("Đã thêm %s vào giỏ hàng!", sp.getName()));
                // Tính lại cart count
                int cartCount = cartRepository.findByUser(user).stream()
                    .mapToInt(Cart::getQuantity)
                    .sum();
                response.put("cartCount", cartCount);
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            }
            
            // Nếu không phải AJAX, redirect như cũ
            return REDIRECT_CART_VIEW;
            
        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Có lỗi xảy ra: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm vào giỏ hàng: " + e.getMessage());
            return REDIRECT_CART_VIEW;
        }
    }

    @GetMapping("/view")
    public String view(Model model, Principal principal) {
        try {
            // Get current user's cart from database
            List<Cart> userCart = getCurrentUserCart(principal);
            
            // Create list of cart items with loaded products for display
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (Cart item : userCart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                    
                    java.math.BigDecimal price = new java.math.BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
                }
            }
            
            model.addAttribute("cartItems", displayItems);
            model.addAttribute("total", total);

            return "Cart/cart";
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @RequestParam int sl,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (sl <= 0) {
                redirectAttributes.addFlashAttribute("error", "Số lượng phải lớn hơn 0!");
                return REDIRECT_CART_VIEW;
            }
            
            if (principal == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập!");
                return "redirect:/auth/login";
            }
            
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/auth/login";
            }
            
            // Tìm sản phẩm để kiểm tra tồn kho
            Products product = repo.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
                return REDIRECT_CART_VIEW;
            }
            
            // Kiểm tra tồn kho
            if (sl > product.getQuantity()) {
                redirectAttributes.addFlashAttribute("error", 
                    String.format("Không đủ hàng trong kho! Chỉ còn %d sản phẩm.", product.getQuantity()));
                return REDIRECT_CART_VIEW;
            }
            
            // Get current user's cart from database
            List<Cart> userCart = cartRepository.findByUser(user);
            
            boolean updated = false;
            for(Cart c : userCart){
                if(c.getProduct().getProductID().equals(id)){
                    c.setQuantity(sl);
                    cartRepository.save(c); // Cập nhật database
                    updated = true;
                    break;
                }
            }
            
            if (updated) {
                // Không gửi thông báo success nữa
                // redirectAttributes.addFlashAttribute("success", 
                //     String.format("Đã cập nhật số lượng sản phẩm '%s' thành %d!", product.getName(), sl));
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm trong giỏ hàng!");
            }
            
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật giỏ hàng: " + e.getMessage());
            return REDIRECT_CART_VIEW;
        }
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, Principal principal) {
        try {
            if (principal == null) return REDIRECT_ERROR;
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) return REDIRECT_ERROR;
            
            // Get current user's cart from database
            List<Cart> userCart = cartRepository.findByUser(user);
            
            Cart itemToRemove = null;
            for (Cart c : userCart) {
                if (c.getProduct().getProductID().equals(id)) {
                    itemToRemove = c;
                    break;
                }
            }
            
            if (itemToRemove != null) {
                cartRepository.delete(itemToRemove); // Xóa khỏi database
            }
            
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/clear")
    public String clear(Principal principal) {
        try {
            if (principal != null) {
                String username = principal.getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    // Xóa tất cả cart items của user từ database
                    List<Cart> userCartItems = cartRepository.findByUser(user);
                    cartRepository.deleteAll(userCartItems);
                }
            }
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {
        try {
            // Get current user's cart from database
            List<Cart> userCart = getCurrentUserCart(principal);
            
            if (userCart == null || userCart.isEmpty()) {
                return REDIRECT_CART_VIEW;
            }
            
            // Create display items for checkout
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (Cart item : userCart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                    java.math.BigDecimal price = new java.math.BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
                }
            }
            
            model.addAttribute("cartItems", displayItems);
            model.addAttribute(ORDER, new Order());
            model.addAttribute("total", total);
            
            return "Cart/checkout";
        } catch (Exception e) {
            model.addAttribute(ERROR, e.getMessage());
            return ERROR;
        }
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("address") String address,
                                  @RequestParam("city") String city,
                                  @RequestParam("region") String region,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam("paymentMethod") String paymentMethod,
                                  Model model,
                                  Principal principal) {
        try {
            // Get current user's cart from database
            List<Cart> userCart = getCurrentUserCart(principal);
            
            if (userCart == null || userCart.isEmpty()) {
                return REDIRECT_CART_VIEW;
            }
            
            // Create order object - using new Order entity structure
            Order order = new Order();
            order.setFullName(fullName);
            order.setEmail(email);
            order.setPhone(phone);
            order.setAddress(address);
            order.setShippingAddress(address + ", " + city + ", " + region);
            order.setPaymentMethod(paymentMethod);
            order.setNote(note);
            
            // Create order details from cart
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
                    // TotalPrice is computed column, will be calculated by database
                    
                    orderDetails.add(detail);
                    total += price * cartItem.getQuantity();
                }
            }
            
            // Set order details
            order.setTotalAmount(total);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            
            // Get user for order
            if (principal == null) {
                model.addAttribute(ERROR, "Vui lòng đăng nhập để đặt hàng!");
                return "Cart/checkout";
            }
            
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                model.addAttribute(ERROR, "Không tìm thấy thông tin người dùng!");
                return "Cart/checkout";
            }
            
            // Set user ID and product ID for order before saving
            order.setUserId(user.getUserId());
            // For multiple products, we'll set productId to null or first product
            if (!orderDetails.isEmpty()) {
                order.setProductId(orderDetails.get(0).getProduct().getProductID());
            }
            
            // Save order
            Order savedOrder = orderService.createOrder(order, orderDetails);
            
            // Clear cart after successful order
            cartRepository.deleteAll(userCart);
            
            // Manually set order details to avoid lazy loading issues
            savedOrder.setOrderDetails(orderDetails);
            
            // Clear cart after successful order - xóa khỏi database
            cartRepository.deleteAll(userCart);
            
            // Redirect to success page with order info
            model.addAttribute(ORDER, savedOrder);
            return "Cart/orderDetails";
            
        } catch (Exception e) {
            model.addAttribute(ERROR, "Error processing order: " + e.getMessage());
            return "Cart/checkout";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable String orderId, Model model, Principal principal) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                model.addAttribute(ERROR, "Không tìm thấy đơn hàng!");
                return REDIRECT_CART_VIEW;
            }
            
            // Kiểm tra quyền truy cập - chỉ cho phép xem đơn hàng của chính mình (hoặc admin)
            if (principal != null) {
                String username = principal.getName();
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null && order.getUserId() != null) {
                    if (!order.getUserId().equals(user.getUserId()) && 
                        !user.getRole().name().equals("ADMIN")) {
                        model.addAttribute(ERROR, "Bạn không có quyền xem đơn hàng này!");
                        return REDIRECT_CART_VIEW;
                    }
                }
            }
            
            model.addAttribute(ORDER, order);
            return "Cart/orderDetails";
        } catch (Exception e) {
            model.addAttribute(ERROR, "Có lỗi xảy ra khi tải thông tin đơn hàng: " + e.getMessage());
            return REDIRECT_CART_VIEW;
        }
    }
    
    // Debug endpoint để kiểm tra cart được load từ database
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
        
        // Load từ database
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
    
    // Endpoint để lấy số lượng sản phẩm trong giỏ hàng (cho AJAX)
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        int count = 0;
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<Cart> userCart = cartRepository.findByUser(user);
                count = userCart.stream().mapToInt(Cart::getQuantity).sum();
            }
        }
        response.put("count", count);
        return response;
    }

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
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
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
