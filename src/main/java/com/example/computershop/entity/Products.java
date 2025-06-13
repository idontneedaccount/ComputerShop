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
@JsonIgnoreProperties({"specifications"})
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
            @Column(unique = true, nullable = false,columnDefinition = "nvarchar(255)")
    String productID;
    @Column(columnDefinition = "nvarchar(255)")
    String name;
    @Column(columnDefinition = "nvarchar(255)")
    String description;
    @Column(columnDefinition = "nvarchar(255)")
    String brand;
    @Column(columnDefinition = "bigint")
    BigInteger price;
    @Column(columnDefinition = "int")
    Integer quantity;
    @Column(columnDefinition = "nvarchar(255)")
    String imageURL;
    @Column(columnDefinition = "bit")
    Boolean isActive = true;
    @Column(columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "categories_id",referencedColumnName = "categoryID")
    private Categories categories;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private ProductSpecifications specifications;
}