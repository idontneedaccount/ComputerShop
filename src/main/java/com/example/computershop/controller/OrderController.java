package com.example.computershop.controller;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.Cart;
import com.example.computershop.service.OrderService;
import com.example.computershop.repository.ProductRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

// TEMPORARILY DISABLED TO AVOID CONFLICT WITH CartController
// @Controller
// @RequestMapping("/checkout")
// @SessionAttributes("cart")
public class OrderController {
    private final OrderService orderService;
    private final ProductRepository productRepository;

    public OrderController(OrderService orderService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @GetMapping
    public String showCheckoutForm(Model model) {
        model.addAttribute("order", new Order());
        // Có thể truyền thêm cart items nếu cần
        return "Cart/checkout";
    }

    @PostMapping
    public String processCheckout(@ModelAttribute("order") Order order,
                                  @RequestParam("productIds") List<String> productIds,
                                  @RequestParam("quantities") List<Integer> quantities,
                                  Model model) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < productIds.size(); i++) {
            Products product = productRepository.findById(productIds.get(i)).orElse(null);
            if (product != null) {
                OrderDetail detail = new OrderDetail();
                detail.setProduct(product);
                detail.setQuantity(quantities.get(i));
                Long price = product.getPrice().longValue();
                detail.setUnitPrice(price);
                detail.setTotalPrice(price * quantities.get(i));
                orderDetails.add(detail);
                total += detail.getTotalPrice();
            }
        }
        order.setTotalAmount(total);
        orderService.createOrder(order, orderDetails);
        model.addAttribute("order", order);
        return "Cart/checkout-success";
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("cart") List<Cart> cart) {
        if (cart == null) return 0;
        return cart.stream().mapToInt(Cart::getQuantity).sum();
    }
} 