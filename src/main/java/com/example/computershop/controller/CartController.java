package com.example.computershop.controller;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.service.OrderService;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import java.security.Principal;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
public class CartController {
    private static final String REDIRECT_ERROR = "redirect:/error";
    private static final String REDIRECT_CART_VIEW = "redirect:/cart/view";
    private static final String ORDER = "order";
    private static final String ERROR = "error";
    private final ProductRepository repo;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public CartController(ProductRepository repo, OrderService orderService, CartRepository cartRepository, UserRepository userRepository) {
        this.repo = repo;
        this.orderService = orderService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute("cart")
    public List<Cart> cart() {
        return new ArrayList<>();
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("cart") List<Cart> cart) {
        if (cart == null) return 0;
        return cart.stream().mapToInt(Cart::getQuantity).sum();
    }

    // Helper method to load product for CartItem
    private Products loadProduct(Cart cartItem) {
        return repo.findById(cartItem.getProduct().getProductID()).orElse(null);
    }

    // Display class for template
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

    @RequestMapping(value = "/add/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      @ModelAttribute("cart") List<Cart> cart,
                      Principal principal) {
        try {
            Products sp = repo.findById(id).orElse(null);
            if(sp == null){
                return REDIRECT_ERROR;
            }
            // Lấy user hiện tại
            if (principal == null) return REDIRECT_ERROR;
            String username = principal.getName();
            
            // Lấy user hiện tại từ username
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) return REDIRECT_ERROR;
            // Kiểm tra sản phẩm đã có trong giỏ của user chưa
            for(Cart c : cart){
                if(c.getProduct().getProductID().equals(id) && c.getUser().getUserId().equals(user.getUserId())){
                    c.setQuantity(c.getQuantity()+sl);
                    cartRepository.save(c);
                    return REDIRECT_CART_VIEW;
                }
            }
            // Thêm mới vào giỏ hàng
            Cart newItem = new Cart();
            newItem.setProduct(sp);
            newItem.setQuantity(sl);
            newItem.setUser(user);
            cart.add(newItem);
            cartRepository.save(newItem);
            return REDIRECT_CART_VIEW;
            
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/view")
    public String view(Model model, @ModelAttribute("cart") List<Cart> cart) {
        try {
            // Create list of cart items with loaded products for display
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (Cart item : cart) {
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
                         @ModelAttribute("cart") List<Cart> cart) {
        try {
            cart.forEach(c->{
                if(c.getProduct().getProductID().equals(id)){
                    c.setQuantity(sl);
                }
            });
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, @ModelAttribute("cart") List<Cart> cart) {
        try {
            cart.removeIf(c->c.getProduct().getProductID().equals(id));
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/clear")
    public String clear(@ModelAttribute("cart") List<Cart> cart) {
        try {
            cart.clear();
            return REDIRECT_CART_VIEW;
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }

    @GetMapping("/checkout")
    public String checkout(Model model, @ModelAttribute("cart") List<Cart> cart) {
        try {
            if (cart == null || cart.isEmpty()) {
                return REDIRECT_CART_VIEW;
            }
            
            // Create display items for checkout
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (Cart item : cart) {
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
                                  @ModelAttribute("cart") List<Cart> cart,
                                  Model model,
                                  SessionStatus sessionStatus) {
        try {
            if (cart == null || cart.isEmpty()) {
                return REDIRECT_CART_VIEW;
            }
            
            // Create order object
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
            
            for (Cart cartItem : cart) {
                Products product = loadProduct(cartItem);
                if (product != null) {
                    OrderDetail detail = new OrderDetail();
                    detail.setProduct(product);
                    detail.setQuantity(cartItem.getQuantity());
                    
                    Long price = product.getPrice().longValue();
                    detail.setUnitPrice(price);
                    detail.setTotalPrice(price * cartItem.getQuantity());
                    
                    orderDetails.add(detail);
                    total += detail.getTotalPrice();
                }
            }
            
            // Set order details
            order.setTotalAmount(total);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            
            // Save order
            Order savedOrder = orderService.createOrder(order, orderDetails);
            
            // Manually set order details to avoid lazy loading issues
            savedOrder.setOrderDetails(orderDetails);
            
            // Clear cart after successful order
            cart.clear();
            sessionStatus.setComplete();
            
            // Redirect to success page with order info
            model.addAttribute(ORDER, savedOrder);
            return "Cart/orderDetails";
            
        } catch (Exception e) {
            model.addAttribute(ERROR, "Error processing order: " + e.getMessage());
            return "Cart/checkout";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable String orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return REDIRECT_CART_VIEW;
            }
            model.addAttribute(ORDER, order);
            return "Cart/orderDetails";
        } catch (Exception e) {
            return REDIRECT_ERROR;
        }
    }
}
