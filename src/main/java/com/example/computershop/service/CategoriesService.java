package com.example.computershop.service;

import java.util.List;

import com.example.computershop.entity.Categories;

public interface  CategoriesService {
    List<Categories> getAll();
    Boolean create(Categories categories);
    Categories FindById(String CategoryID);
    Boolean update(Categories categories);
    Boolean delete(String CategoryID);


}
