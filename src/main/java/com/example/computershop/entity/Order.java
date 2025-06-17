package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", columnDefinition = "varchar(255)")
     String id;

    @Column(name = "full_name", nullable = false)
     String fullName;

    @Column(name = "email", nullable = false)
     String email;

    @Column(name = "phone", nullable = false)
     String phone;

    @Column(name = "address", nullable = false)
     String address;

    @Column(name = "shipping_address", nullable = false)
     String shippingAddress;

    @Column(name = "payment_method", nullable = false)
     String paymentMethod;

    @Column(name = "note")
     String note;

    @Column(name = "total_amount", nullable = false)
     Long totalAmount;

    @Column(name = "order_date", nullable = false)
     LocalDateTime orderDate;

    @Column(name = "status", nullable = false)
     String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", nullable = true)
    private Products product;
} 