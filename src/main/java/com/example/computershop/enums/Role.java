package com.example.computershop.enums;

import lombok.Getter;

@Getter
public enum Role {
    Admin("Quản trị viên"),
    ADMIN("Quản trị viên"), // Backward compatibility
    Customer("Người dùng"),
    CUSTOMER("Nguời dùng"), // Backward compatibility
    Shipper("Nhân viên vận chuyển"),
    SHIPPER("Nhân viên vận chuyển"), // Backward compatibility
    Sales("Nhân viên bán hàng"),
    SALES("Nhân viên bán hàng"); // Backward compatibility

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

} 