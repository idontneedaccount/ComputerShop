package com.example.computershop.payment.vnpay;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Payment;
import com.example.computershop.entity.User;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.CartService;
import com.example.computershop.service.CheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý IPN (Instant Payment Notification) từ VNPay
 * Đây là endpoint để VNPay server gửi thông báo kết quả thanh toán
 */
@RestController
@RequestMapping("/payment")
public class VNPayIPNController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private CheckoutService checkoutService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CartRepository cartRepository;

    /**
     * Endpoint nhận thông báo IPN từ VNPay
     * VNPay sẽ POST kết quả thanh toán đến endpoint này
     */
    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> handleIPNNotification(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Lấy tất cả parameters từ VNPay
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    try {
                        fieldName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString());
                        fieldValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());
                        fields.put(fieldName, fieldValue);
                    } catch (UnsupportedEncodingException e) {
                        // Log error but continue processing
                        System.err.println("Error encoding field: " + fieldName);
                    }
                }
            }

            // Lấy secure hash để verify
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Verify signature
            String signValue = VNPayConfig.hashAllFields(fields);
            
            if (signValue.equals(vnp_SecureHash)) {
                // Signature hợp lệ, xử lý kết quả thanh toán
                String vnp_TxnRef = request.getParameter("vnp_TxnRef");
                String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
                String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
                String vnp_Amount = request.getParameter("vnp_Amount");
                String vnp_PayDate = request.getParameter("vnp_PayDate");
                String vnp_BankCode = request.getParameter("vnp_BankCode");
                String vnp_CardType = request.getParameter("vnp_CardType");

                // Tìm payment record
                Payment payment = paymentRepository.findByVnpTxnRef(vnp_TxnRef);
                
                if (payment != null) {
                    // ✅ FIX: Không ghi đè checkout data - lưu VNPay transaction vào field riêng
                    String existingTransactionId = payment.getTransactionId();
                    boolean hasCheckoutData = existingTransactionId != null && existingTransactionId.startsWith("CHECKOUT_DATA:");
                    
                    if (hasCheckoutData) {
                        // Lưu VNPay transaction vào orderInfo thay vì ghi đè transactionId
                        if (vnp_TransactionNo != null) {
                            payment.setOrderInfo("VNP_TXN:" + vnp_TransactionNo);
                            System.out.println("✅ Preserved checkout data, VNPay transaction saved to orderInfo: " + vnp_TransactionNo);
                        }
                    } else {
                        // Nếu chưa có checkout data, lưu như cũ
                        payment.setTransactionId(vnp_TransactionNo);
                        System.out.println("⚠️ No checkout data found, saved VNPay transaction to transactionId: " + vnp_TransactionNo);
                    }
                    
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setVnpResponseCode(vnp_ResponseCode);
                    payment.setVnpTransactionStatus(vnp_TransactionStatus);
                    payment.setVnpSecureHash(vnp_SecureHash);
                    payment.setVnpPayDate(vnp_PayDate);
                    payment.setBankCode(vnp_BankCode);
                    payment.setCardType(vnp_CardType);
                    payment.setOrderInfo(vnp_OrderInfo);
                    
                    // Kiểm tra kết quả thanh toán
                    if (VNPayConfig.SUCCESS_CODE.equals(vnp_TransactionStatus)) {
                        payment.setPaymentStatus("Paid");
                        
                        // ✅ TẠO ORDER VÀ CLEAR CART KHI THANH TOÁN THÀNH CÔNG
                        try {
                            System.out.println("🎉 Payment successful - Creating order and clearing cart...");
                            
                            // 1. Parse checkout data từ Payment.transactionId 
                            String checkoutDataStr = payment.getTransactionId();
                            if (checkoutDataStr != null && checkoutDataStr.startsWith("CHECKOUT_DATA:")) {
                                checkoutDataStr = checkoutDataStr.substring("CHECKOUT_DATA:".length());
                                String[] parts = checkoutDataStr.split("\\|");
                                
                                if (parts.length >= 6) {
                                    // 2. Tạo CheckoutRequest từ checkout data
                                    CheckoutRequest checkoutRequest = new CheckoutRequest();
                                    checkoutRequest.setAlternativeReceiverName(parts[0]);
                                    checkoutRequest.setAlternativeReceiverPhone(parts[1]);
                                    checkoutRequest.setShippingAddress(parts[2]);
                                    checkoutRequest.setCity(parts[3]);
                                    checkoutRequest.setRegion(parts[4]);
                                    checkoutRequest.setPaymentMethod("VNPAY");
                                    
                                    // 3. Lấy User và Cart items
                                    String userId = payment.getUserId().toString();
                                    User user = userRepository.findById(userId).orElse(null);
                                    
                                    if (user != null) {
                                        // 4. Lấy Cart items bằng CartRepository
                                        List<Cart> cartItems = cartRepository.findByUserUserId(userId);
                                        
                                        if (!cartItems.isEmpty()) {
                                            System.out.println("📦 Found " + cartItems.size() + " cart items for user: " + user.getUsername());
                                            
                                            // 5. Tạo Order từ checkout request và cart items
                                            Order order = checkoutService.createOrderWithVoucher(checkoutRequest, cartItems);
                                            
                                            // 6. Update Payment với Order ID  
                                            payment.setOrderId(java.util.UUID.fromString(order.getId()));
                                            
                                            // 7. Clear Cart sau khi tạo Order thành công
                                            cartRepository.deleteByUser(user);
                                            
                                            System.out.println("✅ Order created successfully: " + order.getId());
                                            System.out.println("✅ Cart cleared for user: " + user.getUsername());
                                            System.out.println("✅ Payment updated with orderId: " + order.getId());
                                            
                                        } else {
                                            System.err.println("❌ Cart is empty for user: " + userId);
                                        }
                                    } else {
                                        System.err.println("❌ User not found: " + userId);
                                    }
                                } else {
                                    System.err.println("❌ Invalid checkout data format: " + checkoutDataStr);
                                }
                            } else {
                                System.err.println("❌ No checkout data found in payment: " + payment.getPaymentId());
                            }
                            
                        } catch (Exception e) {
                            System.err.println("❌ Error creating order: " + e.getMessage());
                            e.printStackTrace();
                            // Vẫn mark payment as successful nhưng order cần xử lý manual
                        }
                        
                        response.put("RspCode", "00");
                        response.put("Message", "success");
                        
                        System.out.println("Payment successful for TxnRef: " + vnp_TxnRef + 
                                         ", TransactionNo: " + vnp_TransactionNo +
                                         ", Amount: " + vnp_Amount +
                                         ", BankCode: " + vnp_BankCode);
                    } else {
                        payment.setPaymentStatus("Failed");
                        response.put("RspCode", "00");
                        response.put("Message", "payment failed but confirmed");
                        
                        System.out.println("Payment failed for TxnRef: " + vnp_TxnRef + 
                                         ", ResponseCode: " + vnp_ResponseCode +
                                         ", Message: " + VNPayConfig.getResponseMessage(vnp_ResponseCode));
                    }
                    
                    paymentRepository.save(payment);
                } else {
                    // Không tìm thấy payment record
                    response.put("RspCode", "01");
                    response.put("Message", "Order not found");
                    
                    System.err.println("Payment record not found for TxnRef: " + vnp_TxnRef);
                }
            } else {
                // Invalid signature
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                
                System.err.println("Invalid signature for IPN notification");
            }
            
        } catch (Exception e) {
            // System error
            response.put("RspCode", "99");
            response.put("Message", "System error: " + e.getMessage());
            
            System.err.println("Error processing IPN notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Alternative GET endpoint for testing (VNPay thường dùng POST)
     */
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("VNPay IPN endpoint is healthy");
    }

} 