package com.example.computershop.controller;

import com.example.computershop.entity.Product;
import com.example.computershop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProductController {
    private final ProductRepository repo;
    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }
    @GetMapping("/admin/add-product")
    public String showAddProductForm(Model model){
        model.addAttribute("product", new Product());
        return "admin/add-product";
    }
    @PostMapping("/admin/add-product")
    public String addProduct(@ModelAttribute("product") Product product, BindingResult result, Model model){
        if (result.hasErrors()){
            return "admin/add-product";
        }
        repo.save(product);
        model.addAttribute("message", "Product added successfully");
        model.addAttribute("product", new Product());
        return "admin/add-product";
    }
}
