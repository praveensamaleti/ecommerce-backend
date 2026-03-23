package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.entity.CartItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private static final String USER_ID = "u1";
    private static final String PRODUCT_ID = "p1";

    private Product sampleProduct;
    private CartItem sampleCartItem;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("50.00"))
                .stock(10)
                .images(Arrays.asList("http://example.com/img.jpg"))
                .build();

        sampleCartItem = CartItem.builder()
                .id("c1")
                .userId(USER_ID)
                .productId(PRODUCT_ID)
                .qty(2)
                .build();
    }

    @Test
    void getCart_Empty_ReturnsEmptyDto() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertFalse(result.isHasOutOfStockItems());
    }

    @Test
    void getCart_WithItems_ReturnsEnrichedDto() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(PRODUCT_ID, result.getItems().get(0).getProductId());
        assertEquals("Test Product", result.getItems().get(0).getProductName());
        assertEquals(new BigDecimal("50.00"), result.getItems().get(0).getPrice());
        assertTrue(result.getItems().get(0).isInStock());
        assertEquals(10, result.getItems().get(0).getAvailableStock());
        assertFalse(result.isHasOutOfStockItems());
    }

    @Test
    void addToCart_NewItem_CreatesItem() {
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.addToCart(USER_ID, PRODUCT_ID, 2);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_IncrementsQty() {
        CartItem existingItem = CartItem.builder()
                .id("c1").userId(USER_ID).productId(PRODUCT_ID).qty(3).build();
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(existingItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        cartService.addToCart(USER_ID, PRODUCT_ID, 2);

        verify(cartItemRepository, times(1)).save(argThat(item -> item.getQty() == 5));
    }

    @Test
    void updateCartItem_ExistingItem_UpdatesQty() {
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.of(sampleCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.updateCartItem(USER_ID, PRODUCT_ID, 5);

        assertNotNull(result);
        verify(cartItemRepository).save(argThat(item -> item.getQty() == 5));
    }

    @Test
    void updateCartItem_NotFound_ThrowsException() {
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> cartService.updateCartItem(USER_ID, PRODUCT_ID, 5));
    }

    @Test
    void removeFromCart_DeletesItem() {
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        CartDto result = cartService.removeFromCart(USER_ID, PRODUCT_ID);

        assertNotNull(result);
        verify(cartItemRepository).deleteByUserIdAndProductId(USER_ID, PRODUCT_ID);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void clearCart_DeletesAll() {
        doNothing().when(cartItemRepository).deleteByUserId(USER_ID);

        cartService.clearCart(USER_ID);

        verify(cartItemRepository).deleteByUserId(USER_ID);
    }

    @Test
    void syncCart_AddsOnlyMissingItems() {
        CartSyncRequest.SyncItem newItem = new CartSyncRequest.SyncItem("p2", 1);
        CartSyncRequest.SyncItem existingItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 3);

        when(cartItemRepository.findByUserIdAndProductId(USER_ID, "p2")).thenReturn(Optional.empty());
        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.of(sampleCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(anyString())).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.syncCart(USER_ID, Arrays.asList(newItem, existingItem));

        assertNotNull(result);
        // Only the new item should be saved; existing item on server keeps server qty
        verify(cartItemRepository, times(1)).save(argThat(item -> item.getProductId().equals("p2")));
        verify(cartItemRepository, never()).save(argThat(item -> item.getProductId().equals(PRODUCT_ID)));
    }

    @Test
    void syncCart_OutOfStockItems_FlaggedInDto() {
        Product outOfStockProduct = Product.builder()
                .id(PRODUCT_ID)
                .name("Out of Stock Product")
                .price(new BigDecimal("30.00"))
                .stock(0)
                .images(Collections.emptyList())
                .build();

        when(cartItemRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(outOfStockProduct));

        CartSyncRequest.SyncItem syncItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 2);
        CartDto result = cartService.syncCart(USER_ID, Arrays.asList(syncItem));

        assertNotNull(result);
        assertTrue(result.isHasOutOfStockItems());
        assertFalse(result.getItems().get(0).isInStock());
    }

    @Test
    void buildCartDto_ProductNotFound_ReturnsItemWithoutProductDetails() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(PRODUCT_ID, result.getItems().get(0).getProductId());
        assertNull(result.getItems().get(0).getProductName());
        // item with no product info has inStock=false by default, so hasOutOfStockItems=true
        assertTrue(result.isHasOutOfStockItems());
    }

    @Test
    void buildCartDto_EmptyImages_NullImageUrl() {
        Product productWithNoImages = Product.builder()
                .id(PRODUCT_ID)
                .name("No Image Product")
                .price(new BigDecimal("20.00"))
                .stock(5)
                .images(Collections.emptyList())
                .build();
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithNoImages));

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertNull(result.getItems().get(0).getImageUrl());
        assertTrue(result.getItems().get(0).isInStock());
    }

    @Test
    void buildCartDto_NullStock_TreatedAsOutOfStock() {
        Product productWithNullStock = Product.builder()
                .id(PRODUCT_ID)
                .name("Null Stock Product")
                .price(new BigDecimal("15.00"))
                .stock(null)
                .images(Arrays.asList("img.jpg"))
                .build();
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(productWithNullStock));

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertFalse(result.getItems().get(0).isInStock());
        assertEquals(0, result.getItems().get(0).getAvailableStock());
        assertTrue(result.isHasOutOfStockItems());
    }

    @Test
    void syncCart_NullItems_ReturnsCurrentCart() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.syncCart(USER_ID, null);

        assertNotNull(result);
        verify(cartItemRepository, never()).save(any());
    }
}
