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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private CartService cartService;

    private static final String USER_ID = "u1";
    private static final String PRODUCT_ID = "p1";
    private static final String VARIANT_ID = "v1";

    private Product sampleProduct;
    private CartItem sampleCartItem;
    private ProductVariant sampleVariant;

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

        sampleVariant = ProductVariant.builder()
                .id(VARIANT_ID)
                .product(sampleProduct)
                .sku("P1-RED-M")
                .stock(8)
                .price(new BigDecimal("60.00"))
                .attributes(new HashMap<>(Map.of("color", "Red", "size", "M")))
                .build();
    }

    // ------------------------------------------------------------------
    // getCart
    // ------------------------------------------------------------------

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
    void getCart_WithVariantItem_ResolvesVariantPriceAndStock() {
        CartItem variantCartItem = CartItem.builder()
                .id("c2").userId(USER_ID).productId(PRODUCT_ID).qty(1).variantId(VARIANT_ID).build();
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(variantCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(sampleVariant));

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        CartItemDto item = result.getItems().get(0);
        assertEquals(VARIANT_ID, item.getVariantId());
        // Variant price (60.00) should override product price (50.00)
        assertEquals(new BigDecimal("60.00"), item.getPrice());
        // Variant stock (8) should override product stock (10)
        assertEquals(8, item.getAvailableStock());
        // Variant label should be populated from attributes
        assertNotNull(item.getVariantLabel());
        assertTrue(item.getVariantLabel().contains("Red"));
    }

    // ------------------------------------------------------------------
    // addToCart — no variant
    // ------------------------------------------------------------------

    @Test
    void addToCart_NewItem_CreatesItem() {
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.addToCart(USER_ID, PRODUCT_ID, 2, null);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void addToCart_ExistingItem_IncrementsQty() {
        CartItem existingItem = CartItem.builder()
                .id("c1").userId(USER_ID).productId(PRODUCT_ID).qty(3).build();
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(existingItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        cartService.addToCart(USER_ID, PRODUCT_ID, 2, null);

        verify(cartItemRepository, times(1)).save(argThat(item -> item.getQty() == 5));
    }

    // ------------------------------------------------------------------
    // addToCart — with variant
    // ------------------------------------------------------------------

    @Test
    void addToCart_WithVariantId_NewItem_CreatesItemWithVariantId() {
        when(cartItemRepository.findByUserIdAndProductIdAndVariantId(USER_ID, PRODUCT_ID, VARIANT_ID))
                .thenReturn(Optional.empty());
        CartItem variantItem = CartItem.builder()
                .id("c2").userId(USER_ID).productId(PRODUCT_ID).qty(1).variantId(VARIANT_ID).build();
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(variantItem);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(variantItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(sampleVariant));

        CartDto result = cartService.addToCart(USER_ID, PRODUCT_ID, 1, VARIANT_ID);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(argThat(item ->
                item.getProductId().equals(PRODUCT_ID) && VARIANT_ID.equals(item.getVariantId())));
    }

    @Test
    void addToCart_WithVariantId_ExistingItem_IncrementsQty() {
        CartItem existingVariantItem = CartItem.builder()
                .id("c2").userId(USER_ID).productId(PRODUCT_ID).qty(2).variantId(VARIANT_ID).build();
        when(cartItemRepository.findByUserIdAndProductIdAndVariantId(USER_ID, PRODUCT_ID, VARIANT_ID))
                .thenReturn(Optional.of(existingVariantItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(existingVariantItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(sampleVariant));

        cartService.addToCart(USER_ID, PRODUCT_ID, 3, VARIANT_ID);

        verify(cartItemRepository).save(argThat(item ->
                item.getQty() == 5 && VARIANT_ID.equals(item.getVariantId())));
    }

    // ------------------------------------------------------------------
    // updateCartItem
    // ------------------------------------------------------------------

    @Test
    void updateCartItem_ExistingItem_UpdatesQty() {
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(sampleCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.updateCartItem(USER_ID, PRODUCT_ID, 5, null);

        assertNotNull(result);
        verify(cartItemRepository).save(argThat(item -> item.getQty() == 5));
    }

    @Test
    void updateCartItem_NotFound_ThrowsException() {
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateCartItem(USER_ID, PRODUCT_ID, 5, null));
    }

    // ------------------------------------------------------------------
    // removeFromCart
    // ------------------------------------------------------------------

    @Test
    void removeFromCart_NoVariant_DeletesNoVariantEntry() {
        doNothing().when(cartItemRepository)
                .deleteByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        CartDto result = cartService.removeFromCart(USER_ID, PRODUCT_ID, null);

        assertNotNull(result);
        verify(cartItemRepository).deleteByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void removeFromCart_WithVariantId_DeletesVariantEntry() {
        doNothing().when(cartItemRepository)
                .deleteByUserIdAndProductIdAndVariantId(USER_ID, PRODUCT_ID, VARIANT_ID);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        CartDto result = cartService.removeFromCart(USER_ID, PRODUCT_ID, VARIANT_ID);

        assertNotNull(result);
        verify(cartItemRepository)
                .deleteByUserIdAndProductIdAndVariantId(USER_ID, PRODUCT_ID, VARIANT_ID);
        verify(cartItemRepository, never())
                .deleteByUserIdAndProductIdAndVariantIdIsNull(anyString(), anyString());
    }

    // ------------------------------------------------------------------
    // clearCart
    // ------------------------------------------------------------------

    @Test
    void clearCart_DeletesAll() {
        doNothing().when(cartItemRepository).deleteByUserId(USER_ID);

        cartService.clearCart(USER_ID);

        verify(cartItemRepository).deleteByUserId(USER_ID);
    }

    // ------------------------------------------------------------------
    // syncCart
    // ------------------------------------------------------------------

    @Test
    void syncCart_UpsertsItems_UpdatesExistingQty() {
        CartSyncRequest.SyncItem newItem = new CartSyncRequest.SyncItem("p2", 1, null);
        CartSyncRequest.SyncItem existingItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 3, null);

        CartItem existingCartItem = CartItem.builder()
                .id("c1").userId(USER_ID).productId(PRODUCT_ID).qty(2).build();

        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(existingCartItem));
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, "p2"))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(anyString())).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.syncCart(USER_ID, Arrays.asList(newItem, existingItem));

        assertNotNull(result);
        verify(cartItemRepository).save(argThat(item ->
                item.getProductId().equals(PRODUCT_ID) && item.getQty() == 3));
        verify(cartItemRepository).save(argThat(item ->
                item.getProductId().equals("p2") && item.getQty() == 1));
    }

    @Test
    void syncCart_RemovesServerItemsNotInRequest() {
        CartItem serverOnlyItem = CartItem.builder()
                .id("c2").userId(USER_ID).productId("p_server_only").qty(1).build();
        CartSyncRequest.SyncItem clientItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 2, null);

        when(cartItemRepository.findByUserId(USER_ID))
                .thenReturn(Arrays.asList(sampleCartItem, serverOnlyItem));
        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.of(sampleCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(anyString())).thenReturn(Optional.of(sampleProduct));
        doNothing().when(cartItemRepository)
                .deleteByUserIdAndProductIdAndVariantIdIsNull(anyString(), anyString());

        cartService.syncCart(USER_ID, Arrays.asList(clientItem));

        verify(cartItemRepository)
                .deleteByUserIdAndProductIdAndVariantIdIsNull(USER_ID, "p_server_only");
    }

    @Test
    void syncCart_WithVariantId_UpsertsVariantItem() {
        CartSyncRequest.SyncItem syncItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 2, VARIANT_ID);

        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());
        when(cartItemRepository.findByUserIdAndProductIdAndVariantId(USER_ID, PRODUCT_ID, VARIANT_ID))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> inv.getArgument(0));

        CartItem variantItem = CartItem.builder()
                .id("c2").userId(USER_ID).productId(PRODUCT_ID).qty(2).variantId(VARIANT_ID).build();
        when(cartItemRepository.findByUserId(USER_ID))
                .thenReturn(Collections.emptyList())           // stale-deletion call
                .thenReturn(Arrays.asList(variantItem));       // buildCartDto call
        when(productRepository.findById(anyString())).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById(VARIANT_ID)).thenReturn(Optional.of(sampleVariant));

        CartDto result = cartService.syncCart(USER_ID, Arrays.asList(syncItem));

        assertNotNull(result);
        verify(cartItemRepository).save(argThat(item ->
                item.getProductId().equals(PRODUCT_ID)
                && VARIANT_ID.equals(item.getVariantId())
                && item.getQty() == 2));
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

        when(cartItemRepository.findByUserIdAndProductIdAndVariantIdIsNull(USER_ID, PRODUCT_ID))
                .thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(outOfStockProduct));

        CartSyncRequest.SyncItem syncItem = new CartSyncRequest.SyncItem(PRODUCT_ID, 2, null);
        CartDto result = cartService.syncCart(USER_ID, Arrays.asList(syncItem));

        assertNotNull(result);
        assertTrue(result.isHasOutOfStockItems());
        assertFalse(result.getItems().get(0).isInStock());
    }

    @Test
    void syncCart_NullItems_ReturnsCurrentCart() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(sampleProduct));

        CartDto result = cartService.syncCart(USER_ID, null);

        assertNotNull(result);
        verify(cartItemRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // buildCartDto edge cases
    // ------------------------------------------------------------------

    @Test
    void buildCartDto_ProductNotFound_ReturnsItemWithoutProductDetails() {
        when(cartItemRepository.findByUserId(USER_ID)).thenReturn(Arrays.asList(sampleCartItem));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        CartDto result = cartService.getCart(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(PRODUCT_ID, result.getItems().get(0).getProductId());
        assertNull(result.getItems().get(0).getProductName());
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
}
