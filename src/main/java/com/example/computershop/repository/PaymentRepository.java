package com.example.computershop.repository;

import com.example.computershop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Payment findByOrderId(UUID orderId);
    
    Payment findByVnpTxnRef(String vnpTxnRef);
    
    Payment findByTransactionId(String transactionId);
    
    List<Payment> findByPaymentStatus(String paymentStatus);
    
    List<Payment> findByPaymentMethod(String paymentMethod);
} 