package com.example.computershop.service.category;

import com.example.computershop.entity.Category;

import java.util.List;

public interface ICategotyService {
    Category getCategotyById(Long id);
    Category getCategotyByName(String name);
    List<Category> getAllCategories();
    Category addCategory(Category category);
    Category updateCategory(Category category, Long id);
    void deleteCategoryById(Long id);

}
