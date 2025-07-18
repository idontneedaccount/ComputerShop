package com.example.computershop.service.impl;

import com.example.computershop.entity.Payment;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.service.interfaces.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    
    private final PaymentRepository paymentRepository;

    @Override
    public Payment findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public Payment findByVnpTxnRef(String vnpTxnRef) {
        return paymentRepository.findByVnpTxnRef(vnpTxnRef);
    }

    @Override
    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public <S extends Payment> S save(S entity) {
        return paymentRepository.save(entity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return paymentRepository.existsById(id);
    }

    @Override
    public long count() {
        return paymentRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        paymentRepository.deleteAll();
    }

    @Override
    public List<Payment> findAll(Sort sort) {
        return paymentRepository.findAll(sort);
    }

    @Override
    public List<Payment> findByPaymentStatus(String status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    @Override
    public List<Payment> findByPaymentMethod(String method) {
        return paymentRepository.findByPaymentMethod(method);
    }
} 