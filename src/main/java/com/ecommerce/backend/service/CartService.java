package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductVariant;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    public CartDto getCart(String userId) {
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto addToCart(String userId, String productId, int qty, String variantId) {
        Optional<CartItem> existing = findCartItem(userId, productId, variantId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQty(item.getQty() + qty);
            cartItemRepository.save(item);
        } else {
            cartItemRepository.save(CartItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .qty(qty)
                    .variantId(normaliseVariantId(variantId))
                    .build());
        }
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto updateCartItem(String userId, String productId, int qty, String variantId) {
        CartItem item = findCartItem(userId, productId, variantId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", productId));
        item.setQty(qty);
        cartItemRepository.save(item);
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto removeFromCart(String userId, String productId, String variantId) {
        deleteCartItem(userId, productId, variantId);
        return buildCartDto(userId);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    /**
     * Client-authoritative sync: the incoming list replaces the server state.
     * Items present on the server but absent from the client list are deleted.
     * Items in the client list are upserted (client qty wins).
     */
    @Transactional
    public CartDto syncCart(String userId, List<CartSyncRequest.SyncItem> incomingItems) {
        if (incomingItems == null) return buildCartDto(userId);

        // Build a set of canonical keys from incoming items to detect stale server entries
        Set<CartItemKey> incomingKeys = incomingItems.stream()
                .map(i -> new CartItemKey(i.getProductId(), normaliseVariantId(i.getVariantId())))
                .collect(Collectors.toSet());

        // Remove server items not present in the client list
        cartItemRepository.findByUserId(userId).stream()
                .filter(item -> !incomingKeys.contains(
                        new CartItemKey(item.getProductId(), item.getVariantId())))
                .forEach(item -> deleteCartItem(userId, item.getProductId(), item.getVariantId()));

        // Upsert all incoming items — client qty is authoritative
        for (CartSyncRequest.SyncItem syncItem : incomingItems) {
            String vid = normaliseVariantId(syncItem.getVariantId());
            Optional<CartItem> existing = findCartItem(userId, syncItem.getProductId(), vid);
            if (existing.isPresent()) {
                CartItem item = existing.get();
                item.setQty(syncItem.getQty());
                cartItemRepository.save(item);
            } else {
                cartItemRepository.save(CartItem.builder()
                        .userId(userId)
                        .productId(syncItem.getProductId())
                        .qty(syncItem.getQty())
                        .variantId(vid)
                        .build());
            }
        }
        return buildCartDto(userId);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Finds a cart item using the correct variant-aware lookup.
     * Null / blank variantId → product without variant selection.
     */
    private Optional<CartItem> findCartItem(String userId, String productId, String variantId) {
        String vid = normaliseVariantId(variantId);
        return (vid == null)
                ? cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(userId, productId)
                : cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, productId, vid);
    }

    /** Deletes a cart item using the correct variant-aware method. */
    private void deleteCartItem(String userId, String productId, String variantId) {
        String vid = normaliseVariantId(variantId);
        if (vid == null) {
            cartItemRepository.deleteByUserIdAndProductIdAndVariantIdIsNull(userId, productId);
        } else {
            cartItemRepository.deleteByUserIdAndProductIdAndVariantId(userId, productId, vid);
        }
    }

    /** Converts empty / blank string to null so the DB partial index logic is consistent. */
    private static String normaliseVariantId(String variantId) {
        return (variantId == null || variantId.isBlank()) ? null : variantId;
    }

    /**
     * Builds the full CartDto by enriching each CartItem with live product/variant data.
     */
    private CartDto buildCartDto(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        List<CartItemDto> dtos = items.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            CartItemDto.CartItemDtoBuilder builder = CartItemDto.builder()
                    .productId(item.getProductId())
                    .qty(item.getQty())
                    .variantId(item.getVariantId());

            if (product != null) {
                String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                        ? product.getImages().get(0) : null;

                BigDecimal effectivePrice = product.getPrice();
                int effectiveStock = product.getStock() != null ? product.getStock() : 0;
                String variantLabel = null;

                // Resolve variant-specific price and stock when a variant is selected
                if (item.getVariantId() != null) {
                    ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null) {
                        effectivePrice = ProductService.resolvePrice(variant.getPrice(), product.getPrice());
                        effectiveStock = variant.getStock();
                        variantLabel = ProductService.buildVariantLabel(variant.getAttributes());
                    }
                }

                builder.productName(product.getName())
                        .price(effectivePrice)
                        .imageUrl(imageUrl)
                        .inStock(effectiveStock > 0)
                        .availableStock(effectiveStock)
                        .variantLabel(variantLabel);
            }
            return builder.build();
        }).collect(Collectors.toList());

        boolean hasOutOfStockItems = dtos.stream()
                .anyMatch(dto -> !dto.isInStock() || dto.getAvailableStock() < dto.getQty());

        return CartDto.builder()
                .items(dtos)
                .hasOutOfStockItems(hasOutOfStockItems)
                .build();
    }

    /**
     * Value object used as a Set key to detect stale cart items during sync.
     * Implements equals/hashCode based on productId + variantId.
     */
    private record CartItemKey(String productId, String variantId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CartItemKey other)) return false;
            return Objects.equals(productId, other.productId)
                    && Objects.equals(variantId, other.variantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, variantId);
        }
    }
}
