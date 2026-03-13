package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order information")
public class OrderDto {
    @Schema(description = "Unique identifier of the order", example = "o987654")
    private String id;

    @Schema(description = "ID of the user who placed the order", example = "u1234")
    private String userId;

    @Schema(description = "Date and time when the order was created")
    private LocalDateTime createdAt;

    @Schema(description = "Current status of the order")
    private OrderStatus status;

    @Schema(description = "List of items in the order")
    private List<OrderItemDto> items;

    @Schema(description = "Shipping address for the order")
    private Address shipping;

    @Schema(description = "Billing address for the order")
    private Address billing;

    @Schema(description = "Subtotal before discounts and taxes", example = "199.98")
    private BigDecimal subtotal;

    @Schema(description = "Total discount amount applied", example = "19.99")
    private BigDecimal discount;

    @Schema(description = "Tax amount applied", example = "14.40")
    private BigDecimal tax;

    @Schema(description = "Final total amount for the order", example = "194.39")
    private BigDecimal total;
}
