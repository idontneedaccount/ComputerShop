package com.example.computershop.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "Orders")
public class Order {

    @Id
    @Column(name = "OrderID", length = 255)
    private String orderId;

    @Column(name = "userid", nullable = false, length = 255)
    private String userId;

    @Column(name = "OrderDate")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "Status", length = 50)
    private String status;

    @Column(name = "TotalAmount")
    private Long totalAmount;

    @Column(name = "ShippingAddress", length = 255)
    private String shippingAddress;

    // Quan hệ 1-n với OrderDetails
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "userid", insertable = false, updatable = false)
    private User user;


}

