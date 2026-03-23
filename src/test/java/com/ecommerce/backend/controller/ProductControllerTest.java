package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.ProductService;
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
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    private ProductDto sampleProductDto;

    private UserDetailsImpl adminUser() {
        return new UserDetailsImpl("admin1", "Admin", "admin@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_admin")));
    }

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

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        when(productService.createProduct(any(ProductDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(post("/api/products")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProductDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void updateProduct_ShouldReturnOk() throws Exception {
        when(productService.updateProduct(anyString(), any(ProductDto.class))).thenReturn(sampleProductDto);

        mockMvc.perform(put("/api/products/p1")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleProductDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/p1")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUser()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCategories_ShouldReturn200WithCategoryList() throws Exception {
        mockMvc.perform(get("/api/products/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }
}
