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
public class ProductSpecifications {
    @Id
     String productID;
     String cpu;
     String ram;
     String ssd;
     String vga;
     String screen;
     Float weight;
     String connect;
     String battery;
    @OneToOne
    @MapsId
    @JoinColumn(name = "productID")
    private Products product;
}
