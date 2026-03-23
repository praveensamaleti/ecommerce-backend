package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartDto getCart(String userId) {
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto addToCart(String userId, String productId, int qty) {
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQty(item.getQty() + qty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .qty(qty)
                    .build();
            cartItemRepository.save(item);
        }
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto updateCartItem(String userId, String productId, int qty) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Cart item not found for product: " + productId));
        item.setQty(qty);
        cartItemRepository.save(item);
        return buildCartDto(userId);
    }

    @Transactional
    public CartDto removeFromCart(String userId, String productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        return buildCartDto(userId);
    }

    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Transactional
    public CartDto syncCart(String userId, List<CartSyncRequest.SyncItem> incomingItems) {
        if (incomingItems != null) {
            for (CartSyncRequest.SyncItem syncItem : incomingItems) {
                Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, syncItem.getProductId());
                if (existing.isEmpty()) {
                    CartItem item = CartItem.builder()
                            .userId(userId)
                            .productId(syncItem.getProductId())
                            .qty(syncItem.getQty())
                            .build();
                    cartItemRepository.save(item);
                }
                // Items already on server keep server qty
            }
        }
        return buildCartDto(userId);
    }

    private CartDto buildCartDto(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        List<CartItemDto> dtos = items.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            CartItemDto.CartItemDtoBuilder builder = CartItemDto.builder()
                    .productId(item.getProductId())
                    .qty(item.getQty());
            if (product != null) {
                String imageUrl = (product.getImages() != null && !product.getImages().isEmpty())
                        ? product.getImages().get(0) : null;
                builder.productName(product.getName())
                        .price(product.getPrice())
                        .imageUrl(imageUrl)
                        .inStock(product.getStock() != null && product.getStock() > 0)
                        .availableStock(product.getStock() != null ? product.getStock() : 0);
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
}
