package com.example.computershop.dto;

import com.example.computershop.entity.Products;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSalesDTO {
    private Products product;
    private Long totalSold;

    public ProductSalesDTO(Products product, Long totalSold) {
        this.product = product;
        this.totalSold = totalSold;
    }
}
