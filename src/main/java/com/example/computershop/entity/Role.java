package com.example.computershop.entity;

import lombok.Getter;

@Getter
public enum Role {
    Admin("Quản trị viên"),
    Customer("Người dùng"),
    Shipper("Nhân viên vận chuyển"),
    Sales("Nhân viên bán hàng");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

} 