package com.example.computershop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private static final String CURRENT_PAGE = "currentPage";
    private static final String CATEGORIES = "categories";
    private static final String DISPLAYED_PRODUCTS = "displayedProducts";
    private static final String HAS_MORE_PRODUCTS = "hasMoreProducts";

    public ShopController(CategoriesService categoriesService, 
                         ProductService productService, 
                         ProductSpecificationService specificationService) {
        this.categoriesService = categoriesService;
        this.productService = productService;
        this.specificationService = specificationService;
    }
    
    private static final int PRODUCTS_PER_PAGE = 30;
    
    @GetMapping("/shopping-page")
    public String showShoppingPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String ssd,
            @RequestParam(required = false) String vga,
            @RequestParam(required = false) String screen,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        List<Categories> categories = categoriesService.getAll();
        model.addAttribute(CATEGORIES, categories);
        
        List<String> brands = productService.getDistinctBrands();
        model.addAttribute("brands", brands);

        List<Products> allProducts = productService.getAll();
        
        int startIndex = page * PRODUCTS_PER_PAGE;
        int endIndex = Math.min(startIndex + PRODUCTS_PER_PAGE, allProducts.size());
        List<Products> products = allProducts.subList(startIndex, endIndex);
        
        model.addAttribute(PRODUCTS, products);
        model.addAttribute(TOTAL_PRODUCTS, allProducts.size());
        model.addAttribute(CURRENT_PAGE, page);
        model.addAttribute("productsPerPage", PRODUCTS_PER_PAGE);
        model.addAttribute(HAS_MORE_PRODUCTS, endIndex < allProducts.size());
        model.addAttribute(DISPLAYED_PRODUCTS, endIndex);
        
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
    
    @GetMapping("/load-more-products")
    @ResponseBody
    public Map<String, Object> loadMoreProducts(
            @RequestParam(defaultValue = "1") int page) {

        List<Products> allProducts = productService.getAll();
        int startIndex = page * PRODUCTS_PER_PAGE;
        int endIndex = Math.min(startIndex + PRODUCTS_PER_PAGE, allProducts.size());
        Map<String, Object> response = new HashMap<>();
        
        if (startIndex < allProducts.size()) {
            List<Products> products = allProducts.subList(startIndex, endIndex);
            response.put(PRODUCTS, products);
            response.put(HAS_MORE_PRODUCTS, endIndex < allProducts.size());
            response.put(CURRENT_PAGE, page);
            response.put(TOTAL_PRODUCTS, allProducts.size());
            response.put(DISPLAYED_PRODUCTS, endIndex);
        } else {
            response.put(PRODUCTS, List.of());
            response.put(HAS_MORE_PRODUCTS, false);
            response.put(CURRENT_PAGE, page);
            response.put(TOTAL_PRODUCTS, allProducts.size());
            response.put(DISPLAYED_PRODUCTS, allProducts.size());
        }
        return response;
    }

} 