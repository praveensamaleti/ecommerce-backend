package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Products", description = "Endpoints for viewing and managing products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "List all products", description = "Retrieves a paginated list of products with optional filtering by query, category, and price range")
    public ResponseEntity<ProductListResponse> getAllProducts(
            @Parameter(description = "Search query for product names") @RequestParam(required = false) String query,
            @Parameter(description = "Category to filter by") @RequestParam(required = false) Category category,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int pageSize) {
        
        return ResponseEntity.ok(productService.getAllProducts(query, category, minPrice, maxPrice, page, pageSize));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all product categories", description = "Returns the list of available product categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(
            Arrays.stream(Category.values())
                  .map(Enum::name)
                  .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Returns details of a single product")
    public ResponseEntity<ProductDto> getProductById(@Parameter(description = "ID of the product") @PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Create a new product (Admin only)", description = "Adds a new product to the catalog")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        return new ResponseEntity<>(productService.createProduct(productDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Update an existing product (Admin only)", description = "Modifies an existing product's details")
    public ResponseEntity<ProductDto> updateProduct(@Parameter(description = "ID of the product to update") @PathVariable String id, @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Delete a product (Admin only)", description = "Removes a product from the catalog")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "ID of the product to delete") @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
