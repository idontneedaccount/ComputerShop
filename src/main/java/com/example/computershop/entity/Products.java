package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
@JsonIgnoreProperties({"specifications"})
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "productid", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String productID;
    
    @Column(name = "name", columnDefinition = "nvarchar(255)")
    String name;
    
    @Column(name = "description", columnDefinition = "nvarchar(255)")
    String description;
    
    @Column(name = "brand", columnDefinition = "nvarchar(255)")
    String brand;
    
    @Column(name = "price", columnDefinition = "bigint")
    BigInteger price;
    
    @Column(name = "quantity", columnDefinition = "int")
    Integer quantity;
    
    @Column(name = "imageurl", columnDefinition = "nvarchar(255)")
    String imageURL;
    
    @Column(name = "is_active", columnDefinition = "bit")
    Boolean isActive = true;
    
    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "categories_id", referencedColumnName = "categoryid", columnDefinition = "varchar(255)")
    private Categories categories;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private ProductSpecifications specifications;
}