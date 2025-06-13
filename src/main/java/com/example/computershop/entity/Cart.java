package com.example.computershop.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "Carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cartId")
    private String cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    private transient Products product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Cart(Products product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // getters, setters, constructors
}
