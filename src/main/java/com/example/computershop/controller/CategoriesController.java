package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.service.CategoriesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class CategoriesController {
    private CategoriesService service;
    private static final String CATEGORIES = "categories";
    private static final String CATEGORIES_VIEW = "admin/categories/categories";
    private static final String CATEGORIES2 = "redirect:/admin/categories";
    @GetMapping("/categories")
    public String showCategoriesPage(Model model){
        List<Categories> list = this.service.getAll();
        model.addAttribute(CATEGORIES, list);
        return CATEGORIES_VIEW;
    }
    @RequestMapping("/add-categories")
    public String add(Model model) {
        Categories categories = new Categories();
        model.addAttribute(CATEGORIES, categories);
        return "admin/categories/add";
    }
    @PostMapping("/add-categories")
    public String addCategories(@ModelAttribute("categories") Categories categories , Model model) {
        if (this.service.create(categories)) {
            return CATEGORIES2;
        } else {
            return "admin/categories/add";
        }
    }
    @GetMapping("/edit-categories/{categoryID}")
    public String editCategories( Model model,@PathVariable("categoryID") String categoryID) {
        Categories categories = this.service.findById(categoryID);
            model.addAttribute(CATEGORIES, categories);
            return "admin/categories/edit";
    }
    @PostMapping("/edit-categories")
    public String updateCategories(@ModelAttribute("categories") Categories categories, Model model) {
        if (this.service.update(categories)) {
            return CATEGORIES2;
        } else {
            return "admin/categories/edit";
        }
    }
    @GetMapping("/delete-categories/{categoryID}")
    public String deleteCategories(@PathVariable("categoryID") String categoryID) {
        if (this.service.delete(categoryID)) {
            return CATEGORIES2;
        } else {
            return CATEGORIES_VIEW;
        }
    }
}
