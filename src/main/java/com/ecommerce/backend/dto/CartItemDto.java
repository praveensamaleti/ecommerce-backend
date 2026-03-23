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

    /** Variant selected for this cart item — null if the product has no variants. */
    private String variantId;

    /** Human-readable variant label shown in the cart, e.g. "Red / M". */
    private String variantLabel;
}
