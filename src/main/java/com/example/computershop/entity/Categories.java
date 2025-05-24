package com.example.computershop.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Categories {
    @Id
     public String CategoryID;
     public String Name;
     public String Description;

    public Categories() {
    }

    public Categories(String categoryID, String name, String description) {
        CategoryID = categoryID;
        Name = name;
        Description = description;
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
}
