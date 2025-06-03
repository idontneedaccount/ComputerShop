package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {

    @Id
    @Column(name = "OrderDetailID", length = 255)
    private String orderDetailId;

    @ManyToOne
    @JoinColumn(name = "OrderID", nullable = false)
    private Order order;

    @Column(name = "ProductID", nullable = false, length = 255)
    private String productId;

    @Column(name = "Quantity", nullable = false)
    private int quantity;

    @Column(name = "UnitPrice", nullable = false)
    private long unitPrice;

    @Column(name = "TotalPrice")
    private long totalPrice;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        this.totalPrice = this.quantity * this.unitPrice;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", referencedColumnName = "ProductID", insertable = false, updatable = false)
    private Products product;

}
