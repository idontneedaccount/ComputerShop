package com.example.computershop.entity;

import jakarta.persistence.Entity;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OrderID", columnDefinition = "UNIQUEIDENTIFIER")
     String id;

    @Column(name = "UserID", columnDefinition = "UNIQUEIDENTIFIER")
     String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "productID", columnDefinition = "UNIQUEIDENTIFIER")
     String productId;

    @Column(name = "OrderDate", nullable = false)
     LocalDateTime orderDate;

    @Column(name = "Status", columnDefinition = "NVARCHAR(50)")
     String status;

    @Column(name = "TotalAmount", nullable = false)
     Long totalAmount;

    @Column(name = "ShippingAddress", columnDefinition = "NVARCHAR(255)")
     String shippingAddress;

    @Column(name = "voucherID", columnDefinition = "UNIQUEIDENTIFIER")
     String voucherId;

    // Additional fields for checkout form - these need to be persisted
    @Column(name = "FullName", columnDefinition = "NVARCHAR(255)")
    private String fullName;
    
    @Column(name = "Email", columnDefinition = "NVARCHAR(255)")
    private String email;
    
    @Column(name = "Phone", columnDefinition = "NVARCHAR(50)")
    private String phone;
    
    @Column(name = "Address", columnDefinition = "NVARCHAR(500)")
    private String address;
    
    @Column(name = "PaymentMethod", columnDefinition = "NVARCHAR(50)")
    private String paymentMethod;
    
    @Column(name = "Note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @Column(name = "ShippingMethod", columnDefinition = "NVARCHAR(50)")
    private String shippingMethod;
    
    @Column(name = "ShippingFee")
    private Long shippingFee;
    
    @Column(name = "Distance")
    private Double distance;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productID", referencedColumnName = "productID", columnDefinition = "UNIQUEIDENTIFIER", insertable = false, updatable = false)
    private Products product;
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }
} 
