package com.ecommerce.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartSyncRequest {

    private List<SyncItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncItem {
        private String productId;
        private int qty;
        /** Null for products without variants. */
        private String variantId;
    }
}
