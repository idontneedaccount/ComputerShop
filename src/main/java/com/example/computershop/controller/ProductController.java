package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Product;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequestMapping("/admin")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoriesService categoriesService;
    @Autowired
    private StorageService storageService;

    @RequestMapping("/product")
    public String index(Model model) {
        List<Product> list = this.productService.getAll();
        model.addAttribute("product", list);
        return "admin/product/product";
    }

    @RequestMapping("/add-product")
    public String add(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute("categories", list);
        return "admin/product/add";
    }

    @GetMapping("/product")
    public String showProducts(Model model){
        List<Product> list = this.productService.getAll();
        model.addAttribute("product", list);
        return "admin/product/product";
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute("product") Product product , @RequestParam("productImage") MultipartFile file) {
        //up file
        this.storageService.store(file);
        String fileName = file.getOriginalFilename();
        product.setImageURL(fileName);
        if (this.productService.create(product)) {
            return "redirect:/admin/product";
        }
        return "admin/product/add";
    }
    @GetMapping("/edit-product/{productID}")
    public String editProduct( Model model,@PathVariable("productID") String productID) {
        Product product = this.productService.findById(productID);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute("categories", list);
        model.addAttribute("product", product);
        return "admin/product/edit";
    }
    @PostMapping("/edit-product")
    public String updateProduct(@ModelAttribute("product") Product product, @RequestParam("productImage") MultipartFile file) {
        this.storageService.store(file);
        String fileName = file.getOriginalFilename();
        // Check if the file is empty
        if (file.isEmpty()) {
            // If the file is empty, keep the existing image URL
            Product existingProduct = this.productService.findById(product.getProductID());
            fileName = existingProduct.getImageURL();
            product.setImageURL(fileName);
        }else {
            // If the file is not empty, set the new image URL
            product.setImageURL(fileName);
        }

        if (this.productService.update(product)) {
            return "redirect:/admin/product";
        }
        return "admin/product/edit";
    }
    @GetMapping("/delete-product/{productID}")
    public String deleteProduct(@PathVariable("productID") String productID) {
        if (this.productService.delete(productID)) {
            return "redirect:/admin/product";
        } else {
            return "admin/product/product";
        }
    }
}
