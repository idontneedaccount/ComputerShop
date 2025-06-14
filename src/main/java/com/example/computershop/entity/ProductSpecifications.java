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
    @Column(name = "productid")
    String productID;
    
    @Column(name = "cpu")
    String cpu;
    
    @Column(name = "ram")
    String ram;
    
    @Column(name = "ssd")
    String ssd;
    
    @Column(name = "vga")
    String vga;
    
    @Column(name = "screen")
    String screen;
    
    @Column(name = "weight")
    Float weight;
    
    @Column(name = "connect")
    String connect;
    
    @Column(name = "battery")
    String battery;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "productid")
    private Products product;
}
