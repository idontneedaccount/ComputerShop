package com.example.computershop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity
public class Categories {
    @Id
     public String CategoryID;
     public String Name;
     public String Description;
     @OneToMany(mappedBy = "categories")
     private Set<Product> products;
    public Categories() {
    }

    public Categories(String categoryID , String description, String name, Set<Product>products) {
        CategoryID = categoryID;
        Description = description;
        Name = name;
        this.products = products;
    }

    public String getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(String categoryID) {
        CategoryID = categoryID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }
}
