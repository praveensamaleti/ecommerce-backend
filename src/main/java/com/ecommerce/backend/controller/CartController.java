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
@Tag(name = "Cart", description = "Endpoints for managing the user's shopping cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get cart", description = "Returns the current user's cart with product details and stock status")
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getId()));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart", description = "Adds a product to the cart or increments quantity if already present")
    public ResponseEntity<CartDto> addToCart(@RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails) {
        String productId = (String) body.get("productId");
        int qty = body.containsKey("qty") ? ((Number) body.get("qty")).intValue() : 1;
        return ResponseEntity.ok(cartService.addToCart(userDetails.getId(), productId, qty));
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart item quantity", description = "Sets the quantity of a specific item in the cart")
    public ResponseEntity<CartDto> updateCartItem(@PathVariable String productId,
                                                   @RequestBody Map<String, Object> body,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        int qty = ((Number) body.get("qty")).intValue();
        return ResponseEntity.ok(cartService.updateCartItem(userDetails.getId(), productId, qty));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart", description = "Removes a specific product from the cart")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable String productId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.removeFromCart(userDetails.getId(), productId));
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
    @Operation(summary = "Sync cart", description = "Merges local cart items into the server cart. Items already on server keep server quantity.")
    public ResponseEntity<CartDto> syncCart(@RequestBody CartSyncRequest request,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.syncCart(userDetails.getId(), request.getItems()));
    }
}
