package com.example.computershop.dto.request;

/**
 * DTO for checkout parameters to reduce method parameter count
 */
public class CheckoutRequest {
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String region;
    private String district;
    private String ward;
    private String note;
    private String paymentMethod;
    private String shippingMethod;
    private Double distance;
    private Long shippingFee;

    public CheckoutRequest() {}

    // Getters and setters
    public String getFullName() { 
        return fullName; 
    }
    
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }

    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPhone() { 
        return phone; 
    }
    
    public void setPhone(String phone) { 
        this.phone = phone; 
    }

    public String getAddress() { 
        return address; 
    }
    
    public void setAddress(String address) { 
        this.address = address; 
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
} 