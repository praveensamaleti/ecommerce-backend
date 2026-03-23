package com.ecommerce.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a purchasable variant of a product (e.g. Red / Size M).
 * Each variant has its own stock level and an optional price override.
 * Attributes are stored as a flexible key-value map (color, size, material, etc.).
 */
@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(generator = "custom-id")
    @GenericGenerator(name = "custom-id", strategy = "com.ecommerce.backend.entity.CustomIdGenerator")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    /** Optional stock-keeping unit for inventory management. */
    @Column(unique = true)
    private String sku;

    @Column(nullable = false)
    private Integer stock;

    /**
     * Optional price override. If null, the parent product's price is used.
     * This allows e.g. a larger size to cost more without duplicating the entire product.
     */
    private BigDecimal price;

    /**
     * Flexible attribute map — e.g. {color: "Red", size: "M", material: "Cotton"}.
     * Stored in the variant_attributes collection table.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "variant_attributes",
        joinColumns = @JoinColumn(name = "variant_id")
    )
    @MapKeyColumn(name = "attr_key")
    @Column(name = "attr_value")
    @Builder.Default
    private Map<String, String> attributes = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
