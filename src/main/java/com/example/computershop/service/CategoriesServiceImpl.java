package com.example.computershop.service;
import java.util.List;
import com.example.computershop.entity.Categories;
import com.example.computershop.repository.CategoriesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoriesServiceImpl implements CategoriesService {

    private CategoriesRepository repo;

    @Override
    public List<Categories> getAll() {
        return this.repo.findAll();
    }

    @Override
    public Boolean create(Categories categories) {
        try {
            this.repo.save(categories);
            return true;
        } catch (Exception e) {
           e.printStackTrace();
        }
        return false;
    }

    @Override
    public Categories findById(String categoryID) {
        return this.repo.findById(categoryID).orElse(null);
    }

    @Override
    public Boolean update(Categories categories) {
        try {
            this.repo.save(categories);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean delete(String categoryID) {
        try {
            this.repo.deleteById(categoryID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  false;
    }
}
