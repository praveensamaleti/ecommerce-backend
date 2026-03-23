package com.ecommerce.backend.dto;

import com.ecommerce.backend.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product Data Transfer Object")
public class ProductDto {
    @Schema(description = "Unique identifier of the product", example = "p1001")
    private String id;

    @Schema(description = "Name of the product", example = "Wireless Headphones")
    private String name;

    @Schema(description = "Price of the product", example = "99.99")
    private BigDecimal price;

    @Schema(description = "List of product image URLs")
    private List<String> images;

    @Schema(description = "Category of the product")
    private Category category;

    @Schema(description = "Available stock count", example = "50")
    private Integer stock;

    @Schema(description = "Average user rating", example = "4.5")
    private Double rating;

    @Schema(description = "Total number of user ratings", example = "120")
    private Integer ratingCount;

    @Schema(description = "Detailed description of the product")
    private String description;

    @Schema(description = "Technical specifications of the product")
    private Map<String, String> specs;

    @Schema(description = "List of user reviews")
    private List<ReviewDto> reviews;

    @Schema(description = "Product variants (size, color, etc.); empty list means no variants")
    private List<ProductVariantDto> variants;

    @Schema(description = "Whether the product is featured", example = "true")
    private Boolean featured;
}
