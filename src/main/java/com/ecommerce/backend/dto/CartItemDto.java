package com.ecommerce.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {

    private String productId;
    private int qty;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private boolean inStock;
    private int availableStock;
}
