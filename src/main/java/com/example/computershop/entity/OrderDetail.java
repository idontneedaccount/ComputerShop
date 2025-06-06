package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_detailid")
     String orderDetailID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderid")
     Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productid")
     Products product;

    @Column(name = "quantity", nullable = false)
     Integer quantity;

    @Column(name = "unit_price", nullable = false)
     Long unitPrice;

    @Column(name = "total_price", nullable = false)
     Long totalPrice;
} 