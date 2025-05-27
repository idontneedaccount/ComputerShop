package com.example.computershop.service.category;

import com.example.computershop.entity.Category;
import com.example.computershop.exception.AlreadyExistsException;
import com.example.computershop.exception.ResourceNotFoundException;
import com.example.computershop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategotyService{
    private final CategoryRepository categoryRepository;

    @Override
    public Category getCategotyById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category Not Found!"));
    }

    @Override
    public Category getCategotyByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category addCategory(Category category) {
        return Optional.of(category).filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository :: save).orElseThrow(() -> new AlreadyExistsException(category.getName()+" already exist!"));
    }

    @Override
    public Category updateCategory(Category category, Long id) {
        return Optional.ofNullable(getCategotyById(id)).map(oldCategory ->{
                oldCategory.setName(category.getName());
                return categoryRepository.save(category);
        }) .orElseThrow(() -> new ResourceNotFoundException("Category Not Found!"));
    }

    @Override
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id)
                .ifPresentOrElse(categoryRepository::delete, () -> {
                    throw new ResourceNotFoundException("Category Not Found!");
                });
    }
}
