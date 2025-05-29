package com.example.computershop.controller;

import com.example.computershop.entity.CartItem;
import com.example.computershop.entity.Product;
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

    @PostMapping("/add/{id}")
    public String add(@PathVariable String id, @RequestParam(defaultValue = "1") int sl,
                      @ModelAttribute("cart") List<CartItem> cart) {
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
    }

    @GetMapping("/view")
    public String view(Model model, @ModelAttribute("cart") List<CartItem> cart) {
        model.addAttribute("cartItems", cart);

        // Tính tổng tiền
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (CartItem item : cart) {
            java.math.BigDecimal price = new java.math.BigDecimal(item.getProduct().getPrice().toString());
            int quantity = item.getQuantity();
            total = total.add(price.multiply(java.math.BigDecimal.valueOf(quantity)));
        }
        model.addAttribute("total", total);

        return "Cart/cart";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable String id, @RequestParam int sl,
                         @ModelAttribute("cart") List<CartItem> cart) {
        cart.forEach(c->{
            if(c.getProduct().getProductID().equals(id)){
                c.setQuantity(sl);
            }
        });
        return "redirect:/cart/view";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable String id, @ModelAttribute("cart") List<CartItem> cart) {
        cart.removeIf(c->c.getProduct().getProductID().equals(id));
        return "redirect:/cart/view";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute("cart") List<CartItem> cart) {
        cart.clear();
        return "redirect:/cart/view";
    }
}
