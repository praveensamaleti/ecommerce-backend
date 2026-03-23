package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartDto;
import com.ecommerce.backend.dto.CartItemDto;
import com.ecommerce.backend.dto.CartSyncRequest;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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

    @Test
    void getCart_ShouldReturnCartDto() throws Exception {
        when(cartService.getCart("u1")).thenReturn(sampleCartDto);

        mockMvc.perform(get("/api/cart")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"))
                .andExpect(jsonPath("$.hasOutOfStockItems").value(false));
    }

    @Test
    void addToCart_ShouldReturnUpdatedCart() throws Exception {
        when(cartService.addToCart(anyString(), anyString(), anyInt())).thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("productId", "p1", "qty", 2);

        mockMvc.perform(post("/api/cart/items")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }

    @Test
    void updateCartItem_ShouldReturnUpdatedCart() throws Exception {
        when(cartService.updateCartItem(anyString(), anyString(), anyInt())).thenReturn(sampleCartDto);

        Map<String, Object> body = Map.of("qty", 5);

        mockMvc.perform(put("/api/cart/items/p1")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedCart() throws Exception {
        CartDto emptyCart = CartDto.builder()
                .items(Collections.emptyList())
                .hasOutOfStockItems(false)
                .build();
        when(cartService.removeFromCart(anyString(), anyString())).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/p1")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void clearCart_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/cart")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void syncCart_ShouldReturnMergedCart() throws Exception {
        when(cartService.syncCart(anyString(), anyList())).thenReturn(sampleCartDto);

        CartSyncRequest request = new CartSyncRequest(
                Arrays.asList(new CartSyncRequest.SyncItem("p1", 2))
        );

        mockMvc.perform(post("/api/cart/sync")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value("p1"));
    }
}
