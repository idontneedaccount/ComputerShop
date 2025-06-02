package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    private CategoriesService categoriesService;
    private StorageService storageService;
    private static final String PRODUCT = "product";
    private static final String CATEGORIES = "categories";

    private static final String  PRODUCT_VIEW = "admin/product/product";
    private static final String PRODUCT_VIEW2 = "redirect:/admin/product";

    @RequestMapping("/product")
    public String index(Model model) {
        List<Products> list = this.productService.getAll();
        model.addAttribute(PRODUCT, list);
        return PRODUCT_VIEW;
    }

    @RequestMapping("/add-product")
    public String add(Model model) {
        Products products = new Products();
        model.addAttribute(PRODUCT, products);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, list);
        return "admin/product/add";
    }

    @GetMapping("/product")
    public String showProducts(Model model) {
        List<Products> list = this.productService.getAll();
        model.addAttribute(PRODUCT, list);
        return PRODUCT_VIEW;
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute("product") Products product, @RequestParam("productImage") MultipartFile file) {
        this.storageService.store(file);
        String fileName = file.getOriginalFilename();
        product.setImageURL(fileName);
        if (Boolean.TRUE.equals(this.productService.create(product))) {
            return PRODUCT_VIEW2;
        }
        return "admin/product/add";
    }

    @GetMapping("/edit-product/{productID}")
    public String editProduct(Model model, @PathVariable("productID") String productID) {
        Products products = this.productService.findById(productID);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, list);
        model.addAttribute(PRODUCT, products);
        return "admin/product/edit";
    }

    @PostMapping("/edit-product")
    public String updateProduct(@ModelAttribute("product") Products product, @RequestParam("productImage") MultipartFile file) {
        String fileName;
        if (file.isEmpty()) {
            Products existingProduct = this.productService.findById(product.getProductID());
            fileName = existingProduct.getImageURL();
            product.setImageURL(fileName);
        } else {
            this.storageService.store(file);
            fileName = file.getOriginalFilename();
            product.setImageURL(fileName);
        }

        if (Boolean.TRUE.equals(this.productService.update(product))) {
            return PRODUCT_VIEW2;
        } else {
            return "admin/product/edit";
        }
    }

    @GetMapping("/delete-product/{productID}")
    public String deleteProduct(@PathVariable("productID") String productID) {
        Products product = this.productService.findById(productID);
        if (product!= null){
            String imageURL = product.getImageURL();
            if (imageURL != null && !imageURL.isEmpty()) {
                this.storageService.delete(imageURL);
            }
        }
        if (Boolean.TRUE.equals(this.productService.delete(productID))) {
            return PRODUCT_VIEW2;
        } else {
            return PRODUCT_VIEW;
        }
    }
}
