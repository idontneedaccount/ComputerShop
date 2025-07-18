package com.example.computershop.payment.vnpay;

import com.example.computershop.entity.Payment;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.service.CheckoutService;
import com.example.computershop.service.CartService;
import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.User;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CheckoutService checkoutService;
    
    @Autowired
    private CartService cartService;

    public String createOrder(HttpServletRequest request, int amount, String urlReturn, UUID orderId, UUID userId){
        // Sử dụng constants từ VNPayConfig
        // Tạo vnp_TxnRef unique bằng timestamp + random
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis()).substring(5) + VNPayConfig.getRandomNumber(3);
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        
        System.out.println("🔍 Creating VNPay order:");
        System.out.println("- vnp_TxnRef: " + vnp_TxnRef);
        System.out.println("- Amount: " + amount);
        System.out.println("- OrderId: " + orderId);

        // Payment record sẽ được tạo bởi VNPayController

        // Tạo tham số thanh toán theo chuẩn VNPay API
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        
        // Amount được truyền vào là VND (ví dụ: 44990000)
        // VNPay API yêu cầu nhân 100 để bỏ phần thập phân (44990000 * 100 = 4499000000)
        // Nhưng cần chú ý overflow cho int, nên convert sang long trước
        long vnpayAmount = (long) amount * 100;
        String totalAmount = String.valueOf(vnpayAmount);
        vnp_Params.put("vnp_Amount", totalAmount);
        vnp_Params.put("vnp_CurrCode", VNPayConfig.vnp_CurrCode);
        
        System.out.println("💰 Creating VNPay payment for amount: " + String.format("%,d VND", amount));

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId.toString().replace("-", ""));
        vnp_Params.put("vnp_OrderType", VNPayConfig.vnp_OrderType);
        
        vnp_Params.put("vnp_Locale", VNPayConfig.vnp_Locale);

        // URL return và IPN - phải là absolute URL
        String fullReturnUrl = urlReturn + VNPayConfig.vnp_Returnurl;
        String fullIpnUrl = urlReturn + VNPayConfig.vnp_IpnUrl;
        
        vnp_Params.put("vnp_ReturnUrl", fullReturnUrl);
        vnp_Params.put("vnp_IpnUrl", fullIpnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        // Thời gian tạo và hết hạn
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Tạo query string và secure hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error encoding URL parameters", e);
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String hashString = hashData.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashString);
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        
        System.out.println("✅ VNPay payment URL generated successfully for TxnRef: " + vnp_TxnRef);
        
        return paymentUrl;
    }

    /**
     * Xử lý Return URL từ VNPay (cho hiển thị kết quả cho user)
     * Khác với IPN, đây chỉ để hiển thị kết quả, không cập nhật database
     */
    public int orderReturn(HttpServletRequest request){
        try {
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
                        System.err.println("Error encoding field in return URL: " + fieldName);
                    }
                }
            }

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
                String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
                String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
                String vnp_TxnRef = request.getParameter("vnp_TxnRef");
                String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
                
                // Log thông tin để debug
                System.out.println("VNPay Return - TxnRef: " + vnp_TxnRef + 
                                 ", ResponseCode: " + vnp_ResponseCode + 
                                 ", TransactionStatus: " + vnp_TransactionStatus);
                
                // Kiểm tra kết quả thanh toán
                if (VNPayConfig.isSuccessResponse(vnp_ResponseCode) && 
                    VNPayConfig.SUCCESS_CODE.equals(vnp_TransactionStatus)) {
                    
                    // Thanh toán thành công
                    System.out.println("Payment successful via Return URL: " + vnp_TransactionNo);
                    return 1;
                    
                } else {
                    // Thanh toán thất bại
                    String errorMessage = VNPayConfig.getResponseMessage(vnp_ResponseCode);
                    System.out.println("Payment failed via Return URL: " + errorMessage);
                    return 0;
                }
            } else {
                // Invalid signature
                System.err.println("Invalid signature in VNPay return URL");
                return -1;
            }
        } catch (Exception e) {
            System.err.println("Error processing VNPay return URL: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Tạo order thực sự sau khi thanh toán VNPay thành công
     */
    private void createRealOrderAfterPayment(Payment payment) {
        try {
            // Parse checkout data từ payment (đã lưu trong transactionId field tạm thời)
            String checkoutData = payment.getTransactionId();
            if (checkoutData != null && checkoutData.startsWith("CHECKOUT_DATA:")) {
                String[] parts = checkoutData.substring("CHECKOUT_DATA:".length()).split("\\|");
                if (parts.length >= 6) {
                    String fullName = parts[0];
                    String phone = parts[1];
                    String address = parts[2];
                    String city = parts[3];
                    String region = parts[4];
                    String paymentMethod = parts[5];
                    
                    // Tạo CheckoutRequest từ data đã lưu
                    CheckoutRequest checkoutRequest = new CheckoutRequest();
                    checkoutRequest.setShippingAddress(address);
                    checkoutRequest.setCity(city);
                    checkoutRequest.setRegion(region);
                    checkoutRequest.setPaymentMethod(paymentMethod);
                    checkoutRequest.setAlternativeReceiverName(fullName);
                    checkoutRequest.setAlternativeReceiverPhone(phone);
                    
                    // TODO: Implement order creation after payment success
                    // Cần implement các method sau trong CartService:
                    // - getUserById(UUID userId)
                    // - getCurrentUserCartByUserId(UUID userId)
                    // Và kiểm tra Order entity có method getOrderId()
                    
                    System.out.println("Payment successful for orderId: " + payment.getOrderId() + 
                                     ", userId: " + payment.getUserId() + 
                                     ", amount: " + payment.getPaidAmount());
                    
                    /*
                    if (payment.getUserId() != null) {
                        try {
                            // TODO: Implement these methods in CartService
                            User user = cartService.getUserById(payment.getUserId());
                            List<Cart> cartItems = cartService.getCurrentUserCartByUserId(payment.getUserId());
                            
                            if (user != null && cartItems != null && !cartItems.isEmpty()) {
                                Order order = checkoutService.processCheckout(checkoutRequest, user, cartItems);
                                payment.setOrderId(UUID.fromString(order.getOrderId()));
                                paymentRepository.save(payment);
                            }
                        } catch (Exception e) {
                            System.err.println("Error creating order: " + e.getMessage());
                        }
                    }
                    */
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating order after payment", e);
        }
    }
} 