package com.example.computershop.service.interfaces;

import com.example.computershop.entity.Payment;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPaymentService {
    
    Payment findByOrderId(UUID orderId);
    
    Payment findByVnpTxnRef(String vnpTxnRef);
    
    Payment findByTransactionId(String transactionId);

    List<Payment> findAll();

    <S extends Payment> S save(S entity);

    Optional<Payment> findById(UUID id);

    boolean existsById(UUID id);

    long count();

    void deleteById(UUID id);

    void deleteAll();

    List<Payment> findAll(Sort sort);
    
    List<Payment> findByPaymentStatus(String status);
    
    List<Payment> findByPaymentMethod(String method);
} 