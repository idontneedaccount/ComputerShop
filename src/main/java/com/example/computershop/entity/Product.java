package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
public class Product {
    @Id
    String productID;

    String name;
    String description;

    BigInteger price;
    Integer quantity;
    String imageURL;

    Boolean isActive = true;
    LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "categories_id",referencedColumnName = "categoryID")
    Categories categories;
}
