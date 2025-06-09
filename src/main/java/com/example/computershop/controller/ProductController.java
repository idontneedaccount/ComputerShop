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

import java.math.BigInteger;
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
    private static final String ERROR = "error";

    private static final String  PRODUCT_VIEW = "admin/product/product";
    private static final String PRODUCT_VIEW2 = "redirect:/admin/product";
    private static final String PRODUCT_ADD = "admin/product/add";
    private static final String PRODUCT_EDIT =  "admin/product/edit";
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
        return PRODUCT_ADD;
    }

    @GetMapping("/product")
    public String showProducts(Model model) {
        List<Products> list = this.productService.getAll();
        model.addAttribute(PRODUCT, list);
        return PRODUCT_VIEW;
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute("product") Products product, 
                           @RequestParam("productImage") MultipartFile file,
                           Model model) {
        if (!product.getBrand().matches("^[a-zA-Z\\s]+$")||product.getPrice().compareTo(BigInteger.ZERO) <= 0|| product.getQuantity() <= 0) {
            model.addAttribute(ERROR, "Thông tin sản phẩm không hợp lệ.");
            List<Categories> categories = this.categoriesService.getAll();
            model.addAttribute(CATEGORIES, categories);
            return PRODUCT_ADD;
        }

        if (productService.existsByName(product.getName())) {
            model.addAttribute(ERROR, "Sản phẩm đã tồn tại.");
            List<Categories> categories = this.categoriesService.getAll();
            model.addAttribute(CATEGORIES, categories);
            return PRODUCT_ADD;
        }

        this.storageService.store(file);
        String fileName = file.getOriginalFilename();
        product.setImageURL(fileName);
        if (Boolean.TRUE.equals(this.productService.create(product))) {
            return PRODUCT_VIEW2;
        }
        return PRODUCT_ADD;
    }

    @GetMapping("/edit-product/{productID}")
    public String editProduct(Model model, @PathVariable("productID") String productID) {
        Products products = this.productService.findById(productID);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, list);
        model.addAttribute(PRODUCT, products);
        return PRODUCT_EDIT;
    }

    @PostMapping("/edit-product")
    public String updateProduct(@ModelAttribute("product") Products product, 
                              @RequestParam("productImage") MultipartFile file,
                              Model model) {
        Products existingProduct = this.productService.findById(product.getProductID());
        if (!product.getBrand().matches("^[a-zA-Z\\s]+$")||product.getPrice().compareTo(BigInteger.ZERO) <= 0|| product.getQuantity() <= 0) {
            model.addAttribute(ERROR, "Thông tin sản phẩm không hợp lệ.");
            List<Categories> categories = this.categoriesService.getAll();
            model.addAttribute(CATEGORIES, categories);
            model.addAttribute(PRODUCT, product);
            return PRODUCT_ADD;
        }
        if (!existingProduct.getName().equals(product.getName()) && 
            productService.existsByName(product.getName())) {
            model.addAttribute(ERROR, "Sản phẩm đã tồn tại.");
            List<Categories> categories = this.categoriesService.getAll();
            model.addAttribute(CATEGORIES, categories);
            model.addAttribute(PRODUCT, product);
            return PRODUCT_EDIT;
        }

        String fileName;
        if (file.isEmpty()) {
            fileName = existingProduct.getImageURL();
            product.setImageURL(fileName);
        } else {
            this.storageService.store(file);
            fileName = file.getOriginalFilename();
            product.setImageURL(fileName);
        }

        if (Boolean.TRUE.equals(this.productService.create(product))) {
            return PRODUCT_VIEW2;
        } else {
            return PRODUCT_EDIT;
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
