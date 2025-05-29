package com.example.computershop.entity;

import com.example.computershop.entity.Product;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Product product;
    private int quantity;

}
