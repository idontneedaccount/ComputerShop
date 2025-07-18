package com.example.computershop.payment.vnpay;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayConfig {
    // URLs
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_Returnurl = "/payment/vnpay-payment-return"; // URL hiển thị kết quả cho user
    public static String vnp_IpnUrl = "/payment/vnpay-ipn"; // URL nhận thông báo từ VNPay server
    
    // Merchant Configuration  
    public static String vnp_TmnCode = "2KN7QHUG"; // Mã terminal
    public static String vnp_HashSecret = "WCU5ABHZPHNMEAPBEAZ1QE1T44XN3RKW"; // Hash secret key
    public static String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    
    // Default Parameters theo VNPay API
    public static String vnp_Version = "2.1.0";
    public static String vnp_Command = "pay";
    public static String vnp_CurrCode = "VND";
    public static String vnp_Locale = "vn";
    public static String vnp_OrderType = "other";
    
    // Bank Codes
    public static String VNPAYQR = "VNPAYQR"; // Thanh toán quét mã QR
    public static String VNBANK = "VNBANK"; // Thẻ ATM - Tài khoản ngân hàng nội địa  
    public static String INTCARD = "INTCARD"; // Thẻ thanh toán quốc tế
    
    // Response Codes
    public static String SUCCESS_CODE = "00";
    public static String FRAUD_SUSPECT_CODE = "07";
    public static String INVALID_CARD_CODE = "09";
    public static String AUTH_FAIL_CODE = "10";
    public static String TIMEOUT_CODE = "11";
    public static String CARD_LOCKED_CODE = "12";
    public static String WRONG_OTP_CODE = "13";
    public static String USER_CANCEL_CODE = "24";
    public static String INSUFFICIENT_FUND_CODE = "51";
    public static String EXCEED_LIMIT_CODE = "65";
    public static String BANK_MAINTENANCE_CODE = "75";
    public static String WRONG_PASSWORD_CODE = "79";
    public static String OTHER_ERROR_CODE = "99";

    /**
     * Tạo hash cho tất cả các fields
     */
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    /**
     * Mã hóa HMAC SHA512
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key and data cannot be null");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error generating HMAC SHA512", ex);
        }
    }

    /**
     * Lấy địa chỉ IP của client
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getHeader("X-REAL-IP");
            }
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            // Nếu có nhiều IP, lấy IP đầu tiên
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }
        } catch (Exception e) {
            ipAddress = "127.0.0.1"; // Fallback IP
        }
        return ipAddress;
    }

    /**
     * Tạo số ngẫu nhiên
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Kiểm tra mã response có thành công hay không
     */
    public static boolean isSuccessResponse(String responseCode) {
        return SUCCESS_CODE.equals(responseCode);
    }

    /**
     * Lấy message từ response code
     */
    public static String getResponseMessage(String responseCode) {
        if (SUCCESS_CODE.equals(responseCode)) {
            return "Giao dịch thành công";
        } else if (FRAUD_SUSPECT_CODE.equals(responseCode)) {
            return "Giao dịch bị nghi ngờ gian lận";
        } else if (INVALID_CARD_CODE.equals(responseCode)) {
            return "Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
        } else if (AUTH_FAIL_CODE.equals(responseCode)) {
            return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
        } else if (TIMEOUT_CODE.equals(responseCode)) {
            return "Đã hết hạn chờ thanh toán";
        } else if (CARD_LOCKED_CODE.equals(responseCode)) {
            return "Thẻ/Tài khoản bị khóa";
        } else if (WRONG_OTP_CODE.equals(responseCode)) {
            return "Nhập sai mật khẩu xác thực giao dịch (OTP)";
        } else if (USER_CANCEL_CODE.equals(responseCode)) {
            return "Khách hàng hủy giao dịch";
        } else if (INSUFFICIENT_FUND_CODE.equals(responseCode)) {
            return "Tài khoản không đủ số dư";
        } else if (EXCEED_LIMIT_CODE.equals(responseCode)) {
            return "Tài khoản đã vượt quá hạn mức giao dịch trong ngày";
        } else if (BANK_MAINTENANCE_CODE.equals(responseCode)) {
            return "Ngân hàng thanh toán đang bảo trì";
        } else if (WRONG_PASSWORD_CODE.equals(responseCode)) {
            return "Nhập sai mật khẩu thanh toán quá số lần quy định";
        } else if (OTHER_ERROR_CODE.equals(responseCode)) {
            return "Lỗi không xác định";
        } else {
            return "Lỗi không xác định: " + responseCode;
        }
    }
} 