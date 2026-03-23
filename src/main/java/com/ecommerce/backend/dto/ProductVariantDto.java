package com.ecommerce.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for creating, updating, and reading a product variant.
 * A variant represents a distinct purchasable option of a product,
 * e.g. a T-Shirt in Red / Size M.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A purchasable variant of a product (e.g. Red / Size M)")
public class ProductVariantDto {

    @Schema(description = "Variant ID (null on create, populated on response)", example = "va3f1bc2")
    private String id;

    @Schema(description = "Stock-keeping unit for inventory", example = "SHIRT-RED-M")
    private String sku;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Available stock for this variant", example = "25")
    private Integer stock;

    @Schema(description = "Price override — null means use parent product price", example = "29.99")
    private BigDecimal price;

    @Schema(description = "Variant attributes map", example = "{\"color\": \"Red\", \"size\": \"M\"}")
    private Map<String, String> attributes;

    /**
     * Human-readable label derived from attributes, e.g. "Red / M".
     * Computed server-side; not accepted on input.
     */
    @Schema(description = "Display label derived from attributes", example = "Red / M", accessMode = Schema.AccessMode.READ_ONLY)
    private String label;
}
