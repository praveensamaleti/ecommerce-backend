package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.ecommerce.backend.entity.CustomIdGenerator")
    private String id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer qty;

    /** Variant selected at order time — null for products without variants. */
    @Column(name = "variant_id")
    private String variantId;

    /**
     * Human-readable snapshot of the variant at order time, e.g. "Red / M".
     * Persisted so order history remains accurate even if the variant is later renamed.
     */
    @Column(name = "variant_label")
    private String variantLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
