package com.example.computershop.entity;

import lombok.Getter;


@Getter
public enum Role {
    ADMIN("Quản trị viên"),
    USER("Người dùng"),
    MARKETER("Marketing"),
    SALES("Nhân viên bán hàng");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

} 