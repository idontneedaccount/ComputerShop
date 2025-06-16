package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "product_specifications")
public class ProductSpecifications {
    @Id
    @Column(name = "productid", columnDefinition = "varchar(255)")
    String productID;
    
    @Column(name = "cpu", columnDefinition = "nvarchar(255)")
    String cpu;
    
    @Column(name = "ram", columnDefinition = "nvarchar(255)")
    String ram;
    
    @Column(name = "ssd", columnDefinition = "nvarchar(255)")
    String ssd;
    
    @Column(name = "vga", columnDefinition = "nvarchar(255)")
    String vga;
    
    @Column(name = "screen", columnDefinition = "nvarchar(255)")
    String screen;
    
    @Column(name = "weight", columnDefinition = "float")
    Float weight;
    
    @Column(name = "connect", columnDefinition = "nvarchar(255)")
    String connect;
    
    @Column(name = "battery", columnDefinition = "nvarchar(255)")
    String battery;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "productid", columnDefinition = "varchar(255)")
    private Products product;
}
