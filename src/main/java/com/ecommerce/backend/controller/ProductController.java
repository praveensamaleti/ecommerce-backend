package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.dto.ProductVariantDto;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalogue and variant management")
public class ProductController {

    @Autowired
    private ProductService productService;

    // ------------------------------------------------------------------
    // Product endpoints
    // ------------------------------------------------------------------

    @GetMapping
    @Operation(summary = "List products", description = "Paginated, filterable product catalogue")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @Parameter(description = "Name search") @RequestParam(required = false) String query,
            @Parameter(description = "Category filter") @RequestParam(required = false) Category category,
            @Parameter(description = "Min price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Max price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Page (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(
            productService.getAllProducts(query, category, minPrice, maxPrice, page, pageSize));
    }

    @GetMapping("/categories")
    @Operation(summary = "List categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(
            Arrays.stream(Category.values()).map(Enum::name).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Create product (Admin)")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Update product (Admin)")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id,
                                                     @RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Delete product (Admin)")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Variant sub-resource endpoints
    // ------------------------------------------------------------------

    @GetMapping("/{id}/variants")
    @Operation(summary = "List variants for a product",
               description = "Returns all purchasable variants (size, color, etc.) for a product")
    public ResponseEntity<List<ProductVariantDto>> getVariants(@PathVariable String id) {
        return ResponseEntity.ok(productService.getVariants(id));
    }

    @PostMapping("/{id}/variants")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Add a variant to a product (Admin)",
               description = "Creates a new variant with its own stock, optional price override, and attributes")
    public ResponseEntity<ProductVariantDto> addVariant(
            @PathVariable String id,
            @Valid @RequestBody ProductVariantDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addVariant(id, dto));
    }

    @PutMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Update a variant (Admin)")
    public ResponseEntity<ProductVariantDto> updateVariant(
            @PathVariable String id,
            @PathVariable String variantId,
            @Valid @RequestBody ProductVariantDto dto) {
        return ResponseEntity.ok(productService.updateVariant(id, variantId, dto));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Delete a variant (Admin)")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable String id,
            @PathVariable String variantId) {
        productService.deleteVariant(id, variantId);
        return ResponseEntity.noContent().build();
    }
}
