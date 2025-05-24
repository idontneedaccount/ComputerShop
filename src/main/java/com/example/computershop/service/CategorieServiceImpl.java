package com.example.computershop.service;
import java.util.List;
import com.example.computershop.entity.Categories;
import com.example.computershop.repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategorieServiceImpl implements CategoriesService {
    @Autowired
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
    public Categories FindById(String CategoryID) {
        return this.repo.findById(CategoryID).get();
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
    public Boolean delete(String CategoryID) {
        try {
            this.repo.deleteById(CategoryID);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  false;
    }
}
