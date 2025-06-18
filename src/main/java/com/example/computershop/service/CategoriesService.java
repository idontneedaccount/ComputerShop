package com.example.computershop.service;

import java.util.List;
import com.example.computershop.entity.Categories;
import com.example.computershop.repository.CategoriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
public class CategoriesService {
    private static final Logger logger = LoggerFactory.getLogger(CategoriesService.class);
    private final CategoriesRepository repo;
    private static final String ERROR = "Error creating category: {}";

    public List<Categories> getAll() {
        return this.repo.findAll();
    }

    public Boolean create(Categories categories) {
        try {
            this.repo.save(categories);
            return true;
        } catch (Exception e) {
            logger.error(ERROR, e.getMessage(), e);
        }
        return false;
    }

    public Categories findById(String categoryID) {
        return this.repo.findById(categoryID).orElse(null);
    }

    public Boolean delete(String categoryID) {
        try {
            this.repo.deleteById(categoryID);
            return true;
        } catch (Exception e) {
            logger.error(ERROR, e.getMessage(), e);
        }
        return false;
    }

    public boolean existsByName(String name) {
        return repo.existsByName(name);
    }
}
