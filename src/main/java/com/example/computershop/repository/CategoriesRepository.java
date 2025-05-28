package com.example.computershop.repository;

import com.example.computershop.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, String> {

}
