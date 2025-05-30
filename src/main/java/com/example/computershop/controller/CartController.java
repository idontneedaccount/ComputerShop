package com.example.computershop.controller;

import com.example.computershop.entity.CartItem;
import com.example.computershop.entity.Product;
import com.example.computershop.entity.Order;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
public class CartController {
    @Autowired
    ProductRepository repo;

    @ModelAttribute("cart")
    public List<CartItem> cart() {
        return new ArrayList<>();
    }

    @ModelAttribute("cartCount")
    public int cartCount(@ModelAttribute("cart") List<CartItem> cart) {
        if (cart == null) return 0;
        return cart.stream().mapToInt(CartItem::getQuantity).sum();
    }

    @PostMapping("/add/{id}")
    public String add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      @ModelAttribute("cart") List<CartItem> cart) {
        try {
            Product sp = repo.findById(id).orElse(null);
            if(sp == null){
                return "redirect:/error";
            }
            for(CartItem c : cart){
                if(c.getProduct().getProductID().equals(id)){
                    c.setQuantity(c.getQuantity()+sl);
                    return "redirect:/cart/view";
                }
            }
            cart.add(new CartItem(sp,sl));
            return "redirect:/cart/view";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }

    @GetMapping("/view")
    public String view(Model model, @ModelAttribute("cart") List<CartItem> cart) {
        try {
            model.addAttribute("cartItems", cart);

            // Calculate total
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (CartItem item : cart) {
                if (item != null && item.getProduct() != null && item.getProduct().getPrice() != null) {
                    java.math.BigDecimal price = new java.math.BigDecimal(item.getProduct().getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
                }
            }
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
                if(c.getProduct().getProductID().equals(id)){
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
            cart.removeIf(c->c.getProduct().getProductID().equals(id));
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
            model.addAttribute("cartItems", cart);
            model.addAttribute("order", new Order());
            
            // Calculate total
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (CartItem item : cart) {
                if (item != null && item.getProduct() != null && item.getProduct().getPrice() != null) {
                    java.math.BigDecimal price = new java.math.BigDecimal(item.getProduct().getPrice().toString());
                    int quantity = item.getQuantity();
                    total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
                }
            }
            model.addAttribute("total", total);
            
            return "Cart/checkout";
        } catch (Exception e) {
            return "redirect:/error";
        }
    }
}
