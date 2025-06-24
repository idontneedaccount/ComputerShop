package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "products")
@JsonIgnoreProperties({"specifications", "variants"})
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
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();
    
    // Helper methods để lấy giá min/max từ variants
    public BigInteger getMinPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.stream()
                .filter(v -> v.getIsActive() != null && v.getIsActive())
                .map(ProductVariant::getPrice)
                .min(BigInteger::compareTo)
                .orElse(this.price);
        }
        return this.price;
    }
    
    public BigInteger getMaxPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.stream()
                .filter(v -> v.getIsActive() != null && v.getIsActive())
                .map(ProductVariant::getPrice)
                .max(BigInteger::compareTo)
                .orElse(this.price);
        }
        return this.price;
    }
    
    public boolean hasVariants() {
        return variants != null && !variants.isEmpty();
    }
}