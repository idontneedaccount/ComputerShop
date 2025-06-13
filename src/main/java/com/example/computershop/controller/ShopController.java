package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductSpecificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class ShopController {
    
    private final CategoriesService categoriesService;
    private final ProductService productService;
    private final ProductSpecificationService specificationService;
    
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
        model.addAttribute("categories", categories);
        
        List<String> brands = productService.getDistinctBrands();
        model.addAttribute("brands", brands);

        List<Products> products = productService.getAll();
        model.addAttribute("products", products);
        
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
} 