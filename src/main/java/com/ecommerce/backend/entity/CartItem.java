package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.ecommerce.backend.entity.CustomIdGenerator")
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private int qty;

    /**
     * ID of the selected variant. Null when the product has no variants.
     * DB uniqueness enforced by two partial indexes (see V4 migration):
     *   - UNIQUE(user_id, product_id) WHERE variant_id IS NULL
     *   - UNIQUE(user_id, product_id, variant_id) WHERE variant_id IS NOT NULL
     */
    @Column(name = "variant_id")
    private String variantId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
