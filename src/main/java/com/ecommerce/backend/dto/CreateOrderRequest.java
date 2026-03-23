package com.ecommerce.backend.dto;

import com.ecommerce.backend.entity.Address;
import com.ecommerce.backend.entity.PaymentInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private List<OrderItemRequest> items;
    private Address shipping;
    private Address billing;
    private PaymentInfo payment;

    @Data
    public static class OrderItemRequest {
        private String productId;
        private Integer qty;
        /** Null for products without variants. */
        private String variantId;
    }
}
