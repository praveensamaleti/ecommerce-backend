package com.ecommerce.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {

    private List<CartItemDto> items;
    private boolean hasOutOfStockItems;
}
