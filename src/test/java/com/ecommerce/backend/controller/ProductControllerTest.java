package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        sampleProductDto = ProductDto.builder()
                .id("p1")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .images(new ArrayList<>())
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnOk() throws Exception {
        ProductListResponse response = ProductListResponse.builder()
                .products(new ArrayList<>())
                .totalCount(0L)
                .build();
        
        when(productService.getAllProducts(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById("p1")).thenReturn(sampleProductDto);

        mockMvc.perform(get("/api/products/p1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }
}
