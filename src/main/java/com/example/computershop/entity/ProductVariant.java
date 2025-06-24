package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String variantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "productid", columnDefinition = "nvarchar(255)")
    Products product;
    
    @Column(name = "variant_name", columnDefinition = "nvarchar(255)")
    String variantName; // VD: "Core i5 - 16GB RAM - 512GB SSD"
    
    @Column(name = "price", columnDefinition = "bigint", nullable = false)
    BigInteger price;
    
    @Column(name = "quantity", columnDefinition = "int")
    Integer quantity = 0;
    
    @Column(name = "sku", columnDefinition = "nvarchar(100)")
    String sku; // Mã SKU riêng cho từng variant
    
    @Column(name = "is_active", columnDefinition = "bit")
    Boolean isActive = true;
    
    // Các thuộc tính cấu hình
    @Column(name = "cpu", columnDefinition = "nvarchar(255)")
    String cpu;
    
    @Column(name = "ram", columnDefinition = "nvarchar(255)")
    String ram;
    
    @Column(name = "storage", columnDefinition = "nvarchar(255)")
    String storage;
    
    @Column(name = "gpu", columnDefinition = "nvarchar(255)")
    String gpu;
    
    @Column(name = "screen", columnDefinition = "nvarchar(255)")
    String screen;
    
    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", columnDefinition = "datetime")
    LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method để hiển thị thông tin variant
    public String getDisplayName() {
        StringBuilder name = new StringBuilder();
        if (cpu != null && !cpu.isEmpty()) {
            name.append(cpu);
        }
        if (ram != null && !ram.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(ram);
        }
        if (storage != null && !storage.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(storage);
        }
        if (gpu != null && !gpu.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(gpu);
        }
        return name.length() > 0 ? name.toString() : variantName;
    }
} 