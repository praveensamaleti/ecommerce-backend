package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart management with variant support")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cart", description = "Returns the current user's cart enriched with live product/variant data")
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getId()));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart",
               description = "Adds a product (with optional variant) to the cart. " +
                             "Pass 'variantId' when adding a product that has variants (size/color etc.).")
    public ResponseEntity<CartDto> addToCart(@RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String productId = (String) body.get("productId");
        int qty = body.containsKey("qty") ? ((Number) body.get("qty")).intValue() : 1;
        String variantId = (String) body.getOrDefault("variantId", null);
        return ResponseEntity.ok(cartService.addToCart(userDetails.getId(), productId, qty, variantId));
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart item quantity",
               description = "Sets the quantity for a product/variant combination. " +
                             "Pass 'variantId' as a query param when the item has a variant.")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable String productId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) String variantId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        int qty = ((Number) body.get("qty")).intValue();
        return ResponseEntity.ok(cartService.updateCartItem(userDetails.getId(), productId, qty, variantId));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart",
               description = "Removes a product (or specific variant) from the cart. " +
                             "Pass 'variantId' as a query param to remove a specific variant.")
    public ResponseEntity<CartDto> removeFromCart(
            @PathVariable String productId,
            @RequestParam(required = false) String variantId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.removeFromCart(userDetails.getId(), productId, variantId));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sync cart",
               description = "Client-authoritative sync. Each SyncItem may include an optional 'variantId'. " +
                             "Server removes items absent from the client list.")
    public ResponseEntity<CartDto> syncCart(@RequestBody CartSyncRequest request,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.syncCart(userDetails.getId(), request.getItems()));
    }
}
