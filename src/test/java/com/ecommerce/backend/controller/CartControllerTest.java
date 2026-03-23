package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private CartDto sampleCartDto;
    private UserDetailsImpl sampleUserDetails;

    @BeforeEach
    void setUp() {
        CartItemDto itemDto = CartItemDto.builder()
                .productId("p1")
                .qty(2)
                .productName("Test Product")
                .price(new BigDecimal("50.00"))
                .inStock(true)
                .availableStock(10)
                .build();

        sampleCartDto = CartDto.builder()
                .items(Arrays.asList(itemDto))
                .hasOutOfStockItems(false)
                .build();

        sampleUserDetails = UserDetailsImpl.builder()
                .id("u1")
                .email("user@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_user")))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(UserDetailsImpl user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    // ------------------------------------------------------------------
    // getCart
    // ------------------------------------------------------------------

    @Test
    void getCart_ShouldReturnCartDto() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.getCart("u1")).thenReturn(sampleCartDto);

        mockMvc.perform(get("/api/cart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"))
                .andExpect(jsonPath("$.hasOutOfStockItems").value(false));
    }

    // ------------------------------------------------------------------
    // addToCart — no variant
    // ------------------------------------------------------------------

    @Test
    void addToCart_ShouldReturnUpdatedCart() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.addToCart(anyString(), anyString(), anyInt(), any()))
                .thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("productId", "p1", "qty", 2);

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    void addToCart_WithoutQty_DefaultsToOne() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.addToCart(anyString(), anyString(), anyInt(), any()))
                .thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("productId", "p1");

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    // ------------------------------------------------------------------
    // addToCart — with variantId
    // ------------------------------------------------------------------

    @Test
    void addToCart_WithVariantId_PassesVariantToService() throws Exception {
        setSecurityContext(sampleUserDetails);

        CartItemDto variantItemDto = CartItemDto.builder()
                .productId("p1")
                .qty(1)
                .variantId("v1")
                .productName("Test Product")
                .price(new BigDecimal("60.00"))
                .inStock(true)
                .availableStock(8)
                .build();
        CartDto variantCartDto = CartDto.builder()
                .items(Arrays.asList(variantItemDto))
                .hasOutOfStockItems(false)
                .build();

        when(cartService.addToCart(eq("u1"), eq("p1"), eq(1), eq("v1")))
                .thenReturn(variantCartDto);

        // Use LinkedHashMap to preserve key order for serialization
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productId", "p1");
        body.put("qty", 1);
        body.put("variantId", "v1");

        mockMvc.perform(post("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"))
                .andExpect(jsonPath("$.items[0].variantId").value("v1"));
    }

    // ------------------------------------------------------------------
    // updateCartItem
    // ------------------------------------------------------------------

    @Test
    void updateCartItem_ShouldReturnUpdatedCart() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.updateCartItem(anyString(), anyString(), anyInt(), any()))
                .thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("qty", 5);

        mockMvc.perform(put("/api/cart/items/p1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void updateCartItem_WithVariantId_PassesVariantQueryParamToService() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.updateCartItem(eq("u1"), eq("p1"), eq(3), eq("v1")))
                .thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("qty", 3);

        mockMvc.perform(put("/api/cart/items/p1")
                .param("variantId", "v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    // removeFromCart
    // ------------------------------------------------------------------

    @Test
    void removeFromCart_ShouldReturnUpdatedCart() throws Exception {
        setSecurityContext(sampleUserDetails);
        CartDto emptyCart = CartDto.builder()
                .items(Collections.emptyList())
                .hasOutOfStockItems(false)
                .build();
        when(cartService.removeFromCart(anyString(), anyString(), any())).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/p1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void removeFromCart_WithVariantId_PassesVariantQueryParamToService() throws Exception {
        setSecurityContext(sampleUserDetails);
        CartDto emptyCart = CartDto.builder()
                .items(Collections.emptyList())
                .hasOutOfStockItems(false)
                .build();
        when(cartService.removeFromCart(eq("u1"), eq("p1"), eq("v1"))).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/p1")
                .param("variantId", "v1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ------------------------------------------------------------------
    // clearCart
    // ------------------------------------------------------------------

    @Test
    void clearCart_ShouldReturnNoContent() throws Exception {
        setSecurityContext(sampleUserDetails);
        mockMvc.perform(delete("/api/cart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------
    // syncCart
    // ------------------------------------------------------------------

    @Test
    void syncCart_ShouldReturnMergedCart() throws Exception {
        setSecurityContext(sampleUserDetails);
        when(cartService.syncCart(anyString(), anyList())).thenReturn(sampleCartDto);

        CartSyncRequest request = new CartSyncRequest(
                Arrays.asList(new CartSyncRequest.SyncItem("p1", 2, null))
        );

        mockMvc.perform(post("/api/cart/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    void syncCart_WithVariantId_ShouldReturnMergedCart() throws Exception {
        setSecurityContext(sampleUserDetails);

        CartItemDto variantItemDto = CartItemDto.builder()
                .productId("p1").qty(2).variantId("v1")
                .productName("Test Product").price(new BigDecimal("60.00"))
                .inStock(true).availableStock(8).build();
        CartDto variantCartDto = CartDto.builder()
                .items(Arrays.asList(variantItemDto)).hasOutOfStockItems(false).build();

        when(cartService.syncCart(anyString(), anyList())).thenReturn(variantCartDto);

        CartSyncRequest request = new CartSyncRequest(
                Arrays.asList(new CartSyncRequest.SyncItem("p1", 2, "v1"))
        );

        mockMvc.perform(post("/api/cart/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"))
                .andExpect(jsonPath("$.items[0].variantId").value("v1"));
    }
}
