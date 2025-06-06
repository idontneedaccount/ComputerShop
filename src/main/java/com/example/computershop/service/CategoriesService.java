package com.example.computershop.service;

import java.util.List;
import com.example.computershop.entity.Categories;
import com.example.computershop.repository.CategoriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoriesService {
    private final CategoriesRepository repo;

    public List<Categories> getAll() {
        return this.repo.findAll();
    }

    public Boolean create(Categories categories) {
        try {
            this.repo.save(categories);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Categories findById(String categoryID) {
        return this.repo.findById(categoryID).orElse(null);
    }

    public Boolean update(Categories categories) {
        try {
            this.repo.save(categories);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean delete(String categoryID) {
        try {
            this.repo.deleteById(categoryID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }
}
