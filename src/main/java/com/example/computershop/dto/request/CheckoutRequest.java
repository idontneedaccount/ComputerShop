package com.example.computershop.dto.request;

/**
 * DTO for checkout parameters to reduce method parameter count
 * Updated to remove duplicate fields with User entity
 */
public class CheckoutRequest {
    // ❌ REMOVED - Duplicate với User entity
    // private String fullName;
    // private String email;
    // private String phone;
    // private String address;
    
    // ✅ KEPT - Shipping information (có thể khác với user.address)
    private String shippingAddress;
    private String city;
    private String region;
    private String district;
    private String ward;
    private String note;
    private String paymentMethod;
    private String shippingMethod;
    private Double distance;
    private Long shippingFee;
    
    // ✅ NEW - Alternative receiver information (nếu khác với user)
    private String alternativeReceiverName;
    private String alternativeReceiverPhone;

    public CheckoutRequest() {}

    // Getters and setters for shipping address components
    public String getShippingAddress() { 
        return shippingAddress; 
    }
    
    public void setShippingAddress(String shippingAddress) { 
        this.shippingAddress = shippingAddress; 
    }

    public String getCity() { 
        return city; 
    }
    
    public void setCity(String city) { 
        this.city = city; 
    }

    public String getRegion() { 
        return region; 
    }
    
    public void setRegion(String region) { 
        this.region = region; 
    }

    public String getDistrict() { 
        return district; 
    }
    
    public void setDistrict(String district) { 
        this.district = district; 
    }

    public String getWard() { 
        return ward; 
    }
    
    public void setWard(String ward) { 
        this.ward = ward; 
    }

    public String getNote() { 
        return note; 
    }
    
    public void setNote(String note) { 
        this.note = note; 
    }

    public String getPaymentMethod() { 
        return paymentMethod; 
    }
    
    public void setPaymentMethod(String paymentMethod) { 
        this.paymentMethod = paymentMethod; 
    }

    public String getShippingMethod() { 
        return shippingMethod; 
    }
    
    public void setShippingMethod(String shippingMethod) { 
        this.shippingMethod = shippingMethod; 
    }

    public Double getDistance() { 
        return distance; 
    }
    
    public void setDistance(Double distance) { 
        this.distance = distance; 
    }

    public Long getShippingFee() { 
        return shippingFee; 
    }
    
    public void setShippingFee(Long shippingFee) { 
        this.shippingFee = shippingFee; 
    }

    // Alternative receiver getters and setters
    public String getAlternativeReceiverName() { 
        return alternativeReceiverName; 
    }
    
    public void setAlternativeReceiverName(String alternativeReceiverName) { 
        this.alternativeReceiverName = alternativeReceiverName; 
    }

    public String getAlternativeReceiverPhone() { 
        return alternativeReceiverPhone; 
    }
    
    public void setAlternativeReceiverPhone(String alternativeReceiverPhone) { 
        this.alternativeReceiverPhone = alternativeReceiverPhone; 
    }

    // Helper method để build complete shipping address
    public String getCompleteShippingAddress() {
        StringBuilder address = new StringBuilder();
        if (shippingAddress != null && !shippingAddress.trim().isEmpty()) {
            address.append(shippingAddress);
        }
        if (ward != null && !ward.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(ward);
        }
        if (district != null && !district.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(district);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (region != null && !region.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(region);
        }
        return address.toString();
    }
} 