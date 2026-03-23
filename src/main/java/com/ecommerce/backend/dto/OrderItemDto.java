package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {
    private String productId;
    private String name;
    private BigDecimal price;
    private Integer qty;

    /** Variant that was ordered — null for products without variants. */
    private String variantId;

    /** Snapshot of variant display label at order time, e.g. "Red / M". */
    private String variantLabel;
}
