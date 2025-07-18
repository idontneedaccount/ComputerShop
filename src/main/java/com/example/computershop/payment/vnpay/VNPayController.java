package com.example.computershop.payment.vnpay;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.Payment;
import com.example.computershop.entity.User;
import com.example.computershop.repository.PaymentRepository;
import com.example.computershop.service.CartService;
import com.example.computershop.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/payment")
public class VNPayController {
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private com.example.computershop.service.CheckoutService checkoutService;

    // Hiển thị trang QR Code cho thanh toán VNPay
    @GetMapping("/vnpay/qr")
    public String showVNPayQR(HttpServletRequest request, Model model,
                               @RequestParam("fullName") String fullName,
                               @RequestParam("phone") String phone,
                               @RequestParam("address") String address,
                               @RequestParam("city") String city,
                               @RequestParam("region") String region,
                               @RequestParam(value = "district", required = false) String district,
                               @RequestParam(value = "ward", required = false) String ward,
                               @RequestParam(value = "note", required = false) String note,
                               @RequestParam("paymentMethod") String paymentMethod,
                               @RequestParam("shippingMethod") String shippingMethod,
                               @RequestParam(value = "distance", required = false) Double distance,
                               @RequestParam(value = "shippingFee", required = false) Long shippingFee,
                               Principal principal) {
        
        try {
            // Lấy user hiện tại
            User user = cartService.getUserFromPrincipal(principal);
            if (user == null) {
                model.addAttribute("error", "Vui lòng đăng nhập để tiếp tục thanh toán");
                return "payment/error";
            }
            
            // Lấy cart của user
            List<Cart> cartItems = cartService.getCurrentUserCart(principal);
            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("error", "Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán");
                return "payment/error";
            }
            
            // Tính tổng tiền từ cart
            Map<String, Object> checkoutData = cartService.prepareCheckoutData(principal);
            Long totalAmount = (Long) checkoutData.get("finalTotal");
            
            if (totalAmount == null || totalAmount <= 0) {
                model.addAttribute("error", "Không thể tính tổng tiền đơn hàng");
                return "payment/error";
            }
            
            // Tạo orderId tạm thời (trong thực tế có thể tạo order pending)
            UUID orderId = UUID.randomUUID();
            
            // Lưu thông tin order vào Payment entity để tracking
            // Tạo CheckoutRequest để xử lý sau khi thanh toán thành công
            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setShippingAddress(address);
            checkoutRequest.setCity(city);
            checkoutRequest.setRegion(region);
            checkoutRequest.setDistrict(district);
            checkoutRequest.setWard(ward);
            checkoutRequest.setNote(note);
            checkoutRequest.setPaymentMethod(paymentMethod);
            checkoutRequest.setShippingMethod(shippingMethod);
            checkoutRequest.setDistance(distance);
            checkoutRequest.setShippingFee(shippingFee);
            checkoutRequest.setAlternativeReceiverName(fullName);
            checkoutRequest.setAlternativeReceiverPhone(phone);
            
            // Tạo Payment entity để tracking
            Payment payment = new Payment();
            // paymentId sẽ được tự động generate bởi @GeneratedValue
            payment.setOrderId(orderId);
            payment.setUserId(UUID.fromString(user.getUserId()));
            payment.setPaymentMethod("VNPAY");
            payment.setPaymentStatus("PENDING");
            payment.setPaidAmount(totalAmount);
            payment.setVnpTxnRef(orderId.toString());
            payment.setTransactionId("CHECKOUT_DATA:" + 
                fullName + "|" + phone + "|" + address + "|" + 
                city + "|" + region + "|" + paymentMethod);
            
            // Lưu vào database
            paymentRepository.save(payment);
            
            // Tạo VNPay payment URL sau khi đã lưu Payment
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createOrder(request, totalAmount.intValue(), baseUrl, orderId, UUID.fromString(user.getUserId()));
            
            // Extract vnp_TxnRef từ URL để cập nhật Payment
            try {
                String txnRef = vnpayUrl.substring(vnpayUrl.indexOf("vnp_TxnRef=") + 11);
                txnRef = txnRef.substring(0, txnRef.indexOf("&"));
                payment.setVnpTxnRef(txnRef);
                paymentRepository.save(payment);
                System.out.println("✅ Updated Payment with vnp_TxnRef: " + txnRef);
            } catch (Exception e) {
                System.out.println("⚠️ Could not extract vnp_TxnRef from URL: " + e.getMessage());
            }
            
            // Add thông tin cần thiết vào model
            model.addAttribute("vnpayUrl", vnpayUrl);
            model.addAttribute("orderId", orderId);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);
            model.addAttribute("address", address);
            
            return "payment/qr-payment";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tạo thanh toán: " + e.getMessage());
            return "payment/error";
        }
    }
    
    // API endpoint để kiểm tra trạng thái thanh toán (polling)
    @GetMapping("/vnpay/check-status/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable UUID orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Payment payment = paymentRepository.findByOrderId(orderId);
            if (payment != null) {
                response.put("status", payment.getPaymentStatus());
                response.put("transactionId", payment.getTransactionId());
                response.put("paidAt", payment.getPaidAt());
                response.put("success", true);
            } else {
                response.put("status", "PENDING");
                response.put("success", true);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // Endpoint để user xác nhận và tạo order sau khi thanh toán thành công
    @PostMapping("/vnpay/confirm-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmPaymentAndCreateOrder(@RequestParam String txnRef) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("🌟 =========================");
            System.out.println("🌟 CONFIRM ORDER ENDPOINT CALLED");
            System.out.println("🌟 txnRef received: " + txnRef);
            System.out.println("🌟 =========================");
            
            // Tìm payment by vnp_TxnRef
            System.out.println("🔍 Searching for payment with vnp_TxnRef: " + txnRef);
            Payment payment = paymentRepository.findByVnpTxnRef(txnRef);
            
            if (payment == null) {
                System.err.println("❌ PAYMENT NOT FOUND for txnRef: " + txnRef);
                
                response.put("success", false);
                response.put("message", "Payment not found for txnRef: " + txnRef);
                return ResponseEntity.status(404).body(response);
            }
            
            System.out.println("✅ PAYMENT FOUND!");
            System.out.println("  - Payment ID: " + payment.getPaymentId());
            System.out.println("  - Payment Status: " + payment.getPaymentStatus());
            System.out.println("  - User ID: " + payment.getUserId());
            System.out.println("  - Order ID: " + payment.getOrderId());
            System.out.println("  - Transaction ID: " + payment.getTransactionId());
            
            if (!"PAID".equals(payment.getPaymentStatus())) {
                System.err.println("❌ PAYMENT NOT PAID! Status: " + payment.getPaymentStatus());
                response.put("success", false);
                response.put("message", "Payment not completed yet. Status: " + payment.getPaymentStatus());
                return ResponseEntity.status(400).body(response);
            }
            
            // ✅ FIX: Kiểm tra Order thực sự trong database, không chỉ payment.getOrderId()
            boolean orderActuallyExists = false;
            if (payment.getOrderId() != null) {
                try {
                    Order existingOrder = orderService.getOrderById(payment.getOrderId().toString());
                    orderActuallyExists = (existingOrder != null);
                    System.out.println("🔍 Confirm endpoint - Checked database - Order exists: " + orderActuallyExists + " (OrderId: " + payment.getOrderId() + ")");
                } catch (Exception e) {
                    System.out.println("⚠️ Confirm endpoint - Error checking Order in database: " + e.getMessage());
                    orderActuallyExists = false;
                }
            }
            
            if (orderActuallyExists) {
                System.out.println("✅ ORDER CONFIRMED TO EXIST IN DATABASE: " + payment.getOrderId());
                response.put("success", true);
                response.put("message", "Order already exists");
                response.put("orderId", payment.getOrderId().toString());
                response.put("alreadyExists", true);
                return ResponseEntity.ok(response);
            }
            
            System.out.println("🚀 CREATING NEW ORDER...");
            
            // Tạo order từ payment data
            String orderResult = createOrderFromPayment(payment);
            
            if (orderResult.startsWith("SUCCESS:")) {
                String orderId = orderResult.substring(8); // Remove "SUCCESS:" prefix
                System.out.println("🎉 ORDER CREATION SUCCESS! Order ID: " + orderId);
                response.put("success", true);
                response.put("message", "Order created successfully");
                response.put("orderId", orderId);
                response.put("amount", payment.getPaidAmount());
                response.put("created", true);
            } else {
                System.err.println("❌ ORDER CREATION FAILED: " + orderResult);
                response.put("success", false);
                response.put("message", orderResult);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("💥 CONFIRM ORDER EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Helper method để tạo order từ payment data
     */
    private String createOrderFromPayment(Payment payment) {
        try {
            System.out.println("🚀 Starting createOrderFromPayment for payment: " + payment.getPaymentId());
            
            // Parse checkout data từ Payment.transactionId 
            String checkoutDataStr = payment.getTransactionId();
            CheckoutRequest checkoutRequest = new CheckoutRequest();
            
            if (checkoutDataStr != null && checkoutDataStr.startsWith("CHECKOUT_DATA:")) {
                // ✅ TH1: Có checkout data đầy đủ
                checkoutDataStr = checkoutDataStr.substring("CHECKOUT_DATA:".length());
                String[] parts = checkoutDataStr.split("\\|");
                if (parts.length < 6) {
                    System.err.println("❌ Invalid checkout data format, parts.length: " + parts.length);
                    return "Invalid checkout data format";
                }
                
                String fullName = parts[0];
                String phone = parts[1];  
                String address = parts[2];
                String city = parts[3];
                String region = parts[4];
                String paymentMethod = parts[5];
                
                System.out.println("📦 Parsed checkout data: " + fullName + " | " + phone + " | " + address);
                
                checkoutRequest.setShippingAddress(address);
                checkoutRequest.setCity(city);
                checkoutRequest.setRegion(region);
                checkoutRequest.setPaymentMethod(paymentMethod);
                checkoutRequest.setAlternativeReceiverName(fullName);
                checkoutRequest.setAlternativeReceiverPhone(phone);
                
            } else {
                // ✅ TH2: Payment cũ không có checkout data - tạo fallback
                System.out.println("⚠️ No checkout data found, creating fallback from user info...");
                
                UUID userId = payment.getUserId();
                User user = cartService.getUserById(userId);
                if (user == null) {
                    System.err.println("❌ User not found for userId: " + userId);
                    return "User not found";
                }
                
                // Tạo checkout data mặc định từ user info
                checkoutRequest.setShippingAddress(user.getAddress() != null ? user.getAddress() : "Địa chỉ không có sẵn");
                checkoutRequest.setCity("Hà Nội"); // Default city
                checkoutRequest.setRegion("Miền Bắc"); // Default region
                checkoutRequest.setPaymentMethod("VNPAY");
                checkoutRequest.setAlternativeReceiverName(user.getFullName() != null ? user.getFullName() : user.getUsername());
                checkoutRequest.setAlternativeReceiverPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "0000000000");
                
                System.out.println("📦 Created fallback checkout data for user: " + user.getUsername());
            }
            
            // Lấy user
            UUID userId = payment.getUserId();
            User user = cartService.getUserById(userId);
            if (user == null) {
                System.err.println("❌ User not found for userId: " + userId);
                return "User not found";
            }
            
            System.out.println("👤 Found user: " + user.getUsername() + " (ID: " + user.getUserId() + ")");
            
            // Lấy cart items
            List<Cart> cartItems = cartService.getCurrentUserCartByUserId(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                System.err.println("❌ Cart is empty for user: " + user.getUsername());
                return "Cart is empty - Order cannot be created from paid payment without cart items";
            }
            
            System.out.println("🛒 Found cart items: " + cartItems.size());
            for (Cart item : cartItems) {
                System.out.println("  - " + item.getProduct().getName() + " x" + item.getQuantity());
            }
            
            // ✅ CHÍNH SỬA: Dùng processCheckout thay vì chỉ createOrderWithVoucher
            // processCheckout sẽ tự động: create order -> save order -> create order details -> clear cart
            System.out.println("📝 Creating order using processCheckout...");
            Order savedOrder = checkoutService.processCheckout(checkoutRequest, user, cartItems);
            
            if (savedOrder == null || savedOrder.getId() == null) {
                System.err.println("❌ Failed to save order - savedOrder is null or missing ID");
                return "Failed to save order";
            }
            
            System.out.println("✅ Order created and saved successfully with ID: " + savedOrder.getId());
            
            // Update Payment với Order ID  
            payment.setOrderId(UUID.fromString(savedOrder.getId()));
            paymentRepository.save(payment);
            
            System.out.println("✅ Payment updated with orderId: " + savedOrder.getId());
            System.out.println("✅ Cart automatically cleared by processCheckout");
            System.out.println("🎉 Order creation completed successfully!");
            
            return "SUCCESS:" + savedOrder.getId();
            
        } catch (Exception e) {
            System.err.println("❌ Error creating order from payment: " + e.getMessage());
            e.printStackTrace();
            return "Error creating order: " + e.getMessage();
        }
    }

    // Sau khi hoàn tất thanh toán, VNPAY sẽ chuyển hướng trình duyệt về URL này
    @GetMapping("/vnpay-payment-return")
    public String paymentCompleted(HttpServletRequest request, Model model){
        int paymentStatus = vnPayService.orderReturn(request);

        // Lấy thông tin từ VNPay response
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        String bankCode = request.getParameter("vnp_BankCode");
        String cardType = request.getParameter("vnp_CardType");

        // Format thời gian thanh toán
        if (paymentTime != null && !paymentTime.isEmpty()) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            try {
                LocalDateTime dateTime = LocalDateTime.parse(paymentTime, inputFormatter);
                String formattedDate = dateTime.format(outputFormatter);
                model.addAttribute("paymentTime", formattedDate);
            } catch (Exception e) {
                model.addAttribute("paymentTime", paymentTime);
            }
        }

        // Format số tiền (VNPay trả về amount * 100)
        if (totalPrice != null && totalPrice.length() > 2) {
            try {
                Long amount = Long.parseLong(totalPrice) / 100; // Chia 100 để lấy số tiền gốc
                model.addAttribute("totalPrice", formatCurrency(amount));
            } catch (NumberFormatException e) {
                model.addAttribute("totalPrice", "0 VND");
            }
        }

        // Thêm thông tin chi tiết cho view
        model.addAttribute("orderId", orderInfo);
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("txnRef", txnRef);
        model.addAttribute("bankCode", bankCode);
        model.addAttribute("cardType", cardType);
        model.addAttribute("paymentStatus", paymentStatus);
        
        // Thêm message từ response code
        if (responseCode != null) {
            String responseMessage = VNPayConfig.getResponseMessage(responseCode);
            model.addAttribute("responseCode", responseCode);
            model.addAttribute("responseMessage", responseMessage);
        }

        // Redirect dựa trên kết quả
        if (paymentStatus == 1) {
            // ✅ THANH TOÁN THÀNH CÔNG - Update Payment status
            try {
                System.out.println("🎉 VNPay payment successful - Updating payment status...");
                
                // 1. Update Payment entity status
                Payment payment = paymentRepository.findByVnpTxnRef(txnRef);
                if (payment != null) {
                    payment.setPaymentStatus("PAID");
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setVnpResponseCode(responseCode);
                    payment.setVnpTransactionStatus("Paid");
                    
                    // ✅ FIX: Không ghi đè checkout data - lưu VNPay transaction vào field riêng
                    String existingTransactionId = payment.getTransactionId();
                    boolean hasCheckoutData = existingTransactionId != null && existingTransactionId.startsWith("CHECKOUT_DATA:");
                    
                    if (hasCheckoutData) {
                        // Nếu đã có checkout data, lưu VNPay transaction ID vào orderInfo thay vì transactionId
                        if (transactionId != null) {
                            payment.setOrderInfo("VNP_TXN:" + transactionId);
                            System.out.println("✅ Preserved checkout data, VNPay transaction saved to orderInfo: " + transactionId);
                        }
                    } else {
                        // Nếu chưa có checkout data, lưu VNPay transaction ID như cũ
                        if (transactionId != null) {
                            payment.setTransactionId(transactionId);
                            System.out.println("⚠️ No checkout data found, saved VNPay transaction to transactionId: " + transactionId);
                        }
                    }
                    
                    if (bankCode != null) {
                        payment.setBankCode(bankCode);
                    }
                    if (cardType != null) {
                        payment.setCardType(cardType);
                    }
                    paymentRepository.save(payment);
                    System.out.println("✅ Payment updated successfully: " + payment.getPaymentId());
                    
                    // ✅ TẠO ORDER VÀ CLEAR CART TỰ ĐỘNG
                    try {
                        System.out.println("🚀 Creating order automatically from Return URL...");
                        
                        // ✅ FIX: Kiểm tra Order thực sự trong database, không chỉ payment.getOrderId()
                        boolean orderActuallyExists = false;
                        Order existingOrder = null;
                        
                        if (payment.getOrderId() != null) {
                            // Kiểm tra Order có thực sự tồn tại trong database không
                            try {
                                existingOrder = orderService.getOrderById(payment.getOrderId().toString());
                                orderActuallyExists = (existingOrder != null);
                                System.out.println("🔍 Checked database - Order exists: " + orderActuallyExists + " (OrderId: " + payment.getOrderId() + ")");
                            } catch (Exception e) {
                                System.out.println("⚠️ Error checking Order in database: " + e.getMessage());
                                orderActuallyExists = false;
                            }
                        }
                        
                        if (!orderActuallyExists) {
                            // Tạo Order từ Payment data
                            String orderResult = createOrderFromPayment(payment);
                            
                            if (orderResult.startsWith("SUCCESS:")) {
                                String orderId = orderResult.substring(8); // Remove "SUCCESS:" prefix
                                System.out.println("✅ Order created successfully from Return URL: " + orderId);
                                
                                model.addAttribute("title", "Thanh toán và đặt hàng thành công");
                                model.addAttribute("result", "Thành công");
                                model.addAttribute("paymentId", payment.getPaymentId());
                                model.addAttribute("orderId", orderId);
                                model.addAttribute("hasOrder", true);
                                model.addAttribute("autoCreated", true);
                                
                            } else {
                                System.err.println("❌ Failed to create order from Return URL: " + orderResult);
                                model.addAttribute("title", "Thanh toán thành công");
                                model.addAttribute("result", "Thành công");
                                model.addAttribute("paymentId", payment.getPaymentId());
                                model.addAttribute("hasOrder", false);
                                model.addAttribute("warning", "Thanh toán đã thành công. Đang xử lý đơn hàng, vui lòng đợi...");
                                model.addAttribute("orderError", orderResult);
                            }
                        } else {
                            // Order thực sự đã tồn tại trong database
                            System.out.println("✅ Order confirmed to exist in database: " + payment.getOrderId());
                            model.addAttribute("title", "Thanh toán thành công");
                            model.addAttribute("result", "Thành công");
                            model.addAttribute("paymentId", payment.getPaymentId());
                            model.addAttribute("orderId", payment.getOrderId());
                            model.addAttribute("hasOrder", true);
                        }
                        
                    } catch (Exception orderException) {
                        System.err.println("❌ Exception creating order from Return URL: " + orderException.getMessage());
                        orderException.printStackTrace();
                        
                        // Fallback - thanh toán thành công nhưng Order cần xử lý manual
                        model.addAttribute("title", "Thanh toán thành công");
                        model.addAttribute("result", "Thành công");
                        model.addAttribute("paymentId", payment.getPaymentId());
                        model.addAttribute("hasOrder", false);
                        model.addAttribute("warning", "Thanh toán đã thành công. Đơn hàng đang được xử lý, vui lòng liên hệ hỗ trợ nếu không thấy đơn hàng sau 5 phút.");
                    }
                    
                    return "payment/success";
                } else {
                    System.err.println("❌ Payment not found for txnRef: " + txnRef);
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error processing VNPay success: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Fallback
            model.addAttribute("title", "Thanh toán thành công");
            model.addAttribute("result", "Thành công");
            model.addAttribute("warning", "Thanh toán đã được xác nhận. Đơn hàng đang được xử lý.");
            return "payment/success";
            
        } else if (paymentStatus == 0) {
            model.addAttribute("title", "Thanh toán thất bại");
            model.addAttribute("result", "Thất bại");
            return "payment/failed"; // Trang thất bại
        } else {
            model.addAttribute("title", "Lỗi xác thực thanh toán");
            model.addAttribute("result", "Lỗi");
            model.addAttribute("error", "Không thể xác thực thông tin thanh toán");
            return "payment/error"; // Trang lỗi xác thực
        }
    }


    // Helper method để get username từ userId
    private String getUsernameFromUserId(UUID userId) {
        try {
            // Có thể cần inject UserService hoặc UserRepository ở đây
            // Tạm thời return userId string để không bị lỗi
            return userId.toString();
        } catch (Exception e) {
            System.err.println("Error getting username from userId: " + e.getMessage());
            return null;
        }
    }

    // Utility method để format currency
    private String formatCurrency(long amount) {
        return String.format("%,d VND", amount);
    }


} 