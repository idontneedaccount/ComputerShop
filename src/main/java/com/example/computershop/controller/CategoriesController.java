package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.service.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class CategoriesController {
    @Autowired
    private CategoriesService service;
    @GetMapping("/categories")
    public String showCategoriesPage(Model model){
        List<Categories> list = this.service.getAll();
        model.addAttribute("categories", list);
        return "admin/categories/categories";
    }
    @RequestMapping("/add-categories")
    public String add(Model model) {
        Categories categories = new Categories();
        model.addAttribute("categories", categories);
        return "admin/categories/add";
    }
    @PostMapping("/add-categories")
    public String addCategories(@ModelAttribute("categories") Categories categories , Model model) {
        if (this.service.create(categories)) {
            return "redirect:/admin/categories";
        } else {
            return "admin/categories/add";
        }
    }
    @GetMapping("/edit-categories/{CategoryID}")
    public String editCategories( Model model,@PathVariable("CategoryID") String CategoryID) {
        Categories categories = this.service.FindById(CategoryID);
            model.addAttribute("categories", categories);
            return "admin/categories/edit";
    }
    @PostMapping("/edit-categories")
    public String updateCategories(@ModelAttribute("categories") Categories categories, Model model) {
        if (this.service.update(categories)) {
            return "redirect:/admin/categories";
        } else {
            return "admin/categories/edit";
        }
    }
    @GetMapping("/delete-categories/{CategoryID}")
    public String deleteCategories(@PathVariable("CategoryID") String CategoryID) {
        if (this.service.delete(CategoryID)) {
            return "redirect:/admin/categories";
        } else {
            return "admin/categories/categories";
        }
    }
}
