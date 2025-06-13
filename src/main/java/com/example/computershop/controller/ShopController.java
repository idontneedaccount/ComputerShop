package com.example.computershop.controller;

import java.util.Arrays;
import java.util.List;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductSpecificationService;

@Controller
@RequestMapping("/user")
public class ShopController {
    
    private final CategoriesService categoriesService;
    private final ProductService productService;
    private final ProductSpecificationService specificationService;
    private static final String TOTAL_PRODUCTS = "totalProducts";
    private static final String PRODUCTS = "products";
    private static final String CATEGORIES = "categories";

    public ShopController(CategoriesService categoriesService, 
                         ProductService productService, 
                         ProductSpecificationService specificationService) {
        this.categoriesService = categoriesService;
        this.productService = productService;
        this.specificationService = specificationService;
    }
    
    @GetMapping("/shopping-page")
    public String showShoppingPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String ssd,
            @RequestParam(required = false) String vga,
            @RequestParam(required = false) String screen,
            Model model) {
        
        List<Categories> categories = categoriesService.getAll();
        model.addAttribute(CATEGORIES, categories);
        
        List<String> brands = productService.getDistinctBrands();
        model.addAttribute("brands", brands);

        List<Products> filteredProducts = filterProducts(category, brand);
        
        model.addAttribute(PRODUCTS, filteredProducts);
        model.addAttribute(TOTAL_PRODUCTS, filteredProducts.size());
        
        model.addAttribute("cpus", specificationService.getDistinctCpus());
        model.addAttribute("rams", specificationService.getDistinctRams());
        model.addAttribute("ssds", specificationService.getDistinctSsds());
        model.addAttribute("vgas", specificationService.getDistinctVgas());
        model.addAttribute("screens", specificationService.getDistinctScreens());
        
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedCpu", cpu);
        model.addAttribute("selectedRam", ram);
        model.addAttribute("selectedSsd", ssd);
        model.addAttribute("selectedVga", vga);
        model.addAttribute("selectedScreen", screen);
        
        return "user/shoppingpage";
    }
    
    private List<Products> filterProducts(String category, String brand) {
        List<Products> allProducts = productService.getAll();
        return allProducts.stream()
            .filter(product -> {
                try {
                    Boolean isActive = (Boolean) product.getClass().getMethod("getIsActive").invoke(product);
                    return isActive != null && isActive;
                } catch (Exception e) {
                    return true; // If can't check, include it
                }
            })
            .filter(product -> filterByCategory(product, category))
            .filter(product -> filterByBrand(product, brand))
            .toList();
    }
    
    private boolean filterByCategory(Products product, String categoryFilter) {
        if (categoryFilter == null || categoryFilter.trim().isEmpty()) {
            return true;
        }
        
        try {
            Categories productCategory = (Categories) product.getClass().getMethod("getCategories").invoke(product);
            if (productCategory == null) {
                return false;
            }
            
            String categoryName = (String) productCategory.getClass().getMethod("getName").invoke(productCategory);
            if (categoryName == null) {
                return false;
            }
            
            List<String> categoryNames = Arrays.asList(categoryFilter.split(","));
            return categoryNames.stream()
                .anyMatch(name -> name.trim().equalsIgnoreCase(categoryName));
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean filterByBrand(Products product, String brandFilter) {
        if (brandFilter == null || brandFilter.trim().isEmpty()) {
            return true; // No filter applied
        }
        try {
            String productBrand = (String) product.getClass().getMethod("getBrand").invoke(product);
            if (productBrand == null) {
                return false;
            }
            
            List<String> brandNames = Arrays.asList(brandFilter.split(","));
            return brandNames.stream()
                .anyMatch(brand -> brand.trim().equalsIgnoreCase(productBrand));
        } catch (Exception e) {
            return false;
        }
    }

} 