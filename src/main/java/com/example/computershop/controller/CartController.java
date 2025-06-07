package com.example.computershop.controller;

import com.example.computershop.entity.CartItem;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
public class CartController {
    @Autowired
    ProductRepository repo;
    
    @Autowired
    private OrderService orderService;

    @ModelAttribute("cart")
    public List<CartItem> cart() {
        return new ArrayList<>();
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("cart") List<CartItem> cart) {
        if (cart == null) return 0;
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Helper method to load product for CartItem
    private Products loadProduct(CartItem cartItem) {
        return repo.findById(cartItem.getProductId()).orElse(null);
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
                      @ModelAttribute("cart") List<CartItem> cart) {
        try {
            Products sp = repo.findById(id).orElse(null);
            if(sp == null){
                return "redirect:/error";
            }
            
            // Check if product already exists in cart
            for(CartItem c : cart){
                if(c.getProductId().equals(id)){
                    c.setQuantity(c.getQuantity()+sl);
                    return "redirect:/cart/view";
                }
            }
            
            // Add new item to cart
            CartItem newItem = new CartItem(sp, sl);
            cart.add(newItem);
            return "redirect:/cart/view";
            
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/view")
    public String view(Model model, @ModelAttribute("cart") List<CartItem> cart) {
        try {
            // Create list of cart items with loaded products for display
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (CartItem item : cart) {
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
            return "redirect:/error";
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @RequestParam int sl,
                         @ModelAttribute("cart") List<CartItem> cart) {
        try {
            cart.forEach(c->{
                if(c.getProductId().equals(id)){
                    c.setQuantity(sl);
                }
            });
            return "redirect:/cart/view";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, @ModelAttribute("cart") List<CartItem> cart) {
        try {
            cart.removeIf(c->c.getProductId().equals(id));
            return "redirect:/cart/view";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/clear")
    public String clear(@ModelAttribute("cart") List<CartItem> cart) {
        try {
            cart.clear();
            return "redirect:/cart/view";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/checkout")
    public String checkout(Model model, @ModelAttribute("cart") List<CartItem> cart) {
        try {
            if (cart == null || cart.isEmpty()) {
                return "redirect:/cart/view";
            }
            
            // Create display items for checkout
            List<CartItemDisplay> displayItems = new ArrayList<>();
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            
            for (CartItem item : cart) {
                Products product = loadProduct(item);
                if (product != null) {
                    displayItems.add(new CartItemDisplay(product, item.getQuantity()));
                    java.math.BigDecimal price = new java.math.BigDecimal(product.getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
                }
            }
            
            model.addAttribute("cartItems", displayItems);
            model.addAttribute("order", new Order());
            model.addAttribute("total", total);
            
            return "Cart/checkout";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
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
                                  @ModelAttribute("cart") List<CartItem> cart,
                                  Model model) {
        try {
            if (cart == null || cart.isEmpty()) {
                return "redirect:/cart/view";
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
            
            for (CartItem cartItem : cart) {
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
            
            // Redirect to success page with order info
            model.addAttribute("order", savedOrder);
            return "Cart/orderDetails";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error processing order: " + e.getMessage());
            return "Cart/checkout";
        }
    }

    @GetMapping("/order/{orderId}")
    public String viewOrder(@PathVariable Long orderId, Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return "redirect:/cart/view";
            }
            model.addAttribute("order", order);
            return "Cart/orderDetails";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }
}
