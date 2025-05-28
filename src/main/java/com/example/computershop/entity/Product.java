package com.example.computershop.entity;

import jakarta.persistence.*;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
public class Product {
    @Id
    private String productID;

    private String name;
    private String description;

    private BigInteger price;
    private Integer quantity;
    private String imageURL;

    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "CategoryID", referencedColumnName = "CategoryID")
    private Categories categories;
    public Product() {
    }

    public Product(String productID, String name, String description, BigInteger price, Integer quantity, String imageURL, Boolean isActive, LocalDateTime createdAt, Categories categories) {
        this.productID = productID;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.imageURL = imageURL;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.categories = categories;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigInteger getPrice() {
        return price;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }
}
