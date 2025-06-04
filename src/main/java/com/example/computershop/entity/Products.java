package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "Products")
public class Products {
    @Id
    @Column(name = "productID")
    String productID;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "brand", nullable = false)
    String brand;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    String description;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "imageURL")
    String imageURL;

    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "created_at")
    LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "categories_id", referencedColumnName = "categoryID")
    Categories categories;
}
