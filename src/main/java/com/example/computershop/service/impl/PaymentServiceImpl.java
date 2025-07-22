package com.example.computershop.service.impl;

import com.example.computershop.entity.Payment;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {
    private final PaymentRepository paymentRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public void deleteById(UUID paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    @Override
    public boolean existsById(UUID paymentId) {
        return paymentRepository.existsById(paymentId);
    }

    @Override
    public long count() {
        return paymentRepository.count();
    }

    @Override
    public List<Payment> findAll(Sort sort) {
        return paymentRepository.findAll(sort);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> findByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public Optional<Payment> findByVnpTxnRef(String vnpTxnRef) {
        return paymentRepository.findByVnpTxnRef(vnpTxnRef);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }

    @Override
    public List<Payment> findByPaymentStatus(String paymentStatus) {
        return paymentRepository.findByPaymentStatus(paymentStatus);
    }

    @Override
    public Payment createVNPayPayment(UUID orderId, UUID userId, Long amount, String vnpTxnRef) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setPaymentMethod("VNPAY");
        payment.setPaymentStatus("Pending");
        payment.setPaidAmount(amount);
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updateVNPayPaymentResult(String vnpTxnRef, String vnpResponseCode, 
                                          String vnpTransactionStatus, String vnpPayDate, 
                                          String transactionId, String bankCode, String cardType) {
        Optional<Payment> paymentOpt = paymentRepository.findByVnpTxnRef(vnpTxnRef);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setVnpResponseCode(vnpResponseCode);
            payment.setVnpTransactionStatus(vnpTransactionStatus);
            payment.setVnpPayDate(vnpPayDate);
            payment.setTransactionId(transactionId);
            payment.setBankCode(bankCode);
            payment.setCardType(cardType);
            payment.setUpdatedAt(LocalDateTime.now());
            
            // Update payment status based on VNPay response
            if ("00".equals(vnpResponseCode) && "00".equals(vnpTransactionStatus)) {
                payment.setPaymentStatus("Paid");
                payment.setPaidAt(LocalDateTime.now());
            } else {
                payment.setPaymentStatus("Failed");
            }
            
            return paymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public boolean hasSuccessfulPayment(UUID orderId) {
        return paymentRepository.hasSuccessfulPayment(orderId);
    }

    @Override
    public Long getTotalPaidAmountByOrderId(UUID orderId) {
        return paymentRepository.getTotalPaidAmountByOrderId(orderId);
    }

    @Override
    public List<Payment> findRecentPaymentsByUserId(UUID userId) {
        return paymentRepository.findRecentPaymentsByUserId(userId);
    }
}
