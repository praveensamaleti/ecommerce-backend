package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductDto sampleProductDto;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("p1")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .category(Category.Electronics)
                .stock(10)
                .description("Test Description")
                .featured(true)
                .images(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();

        sampleProductDto = ProductDto.builder()
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .category(Category.Electronics)
                .stock(10)
                .description("Test Description")
                .featured(true)
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnProductList() {
        Page<Product> page = new PageImpl<>(Arrays.asList(sampleProduct));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        ProductListResponse response = productService.getAllProducts(null, null, null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getProducts().size());
        assertEquals(1, response.getTotalCount());
        assertEquals("Test Product", response.getProducts().get(0).getName());
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));

        ProductDto result = productService.getProductById("p1");

        assertNotNull(result);
        assertEquals("p1", result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById("nonexistent"));
    }

    @Test
    void createProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto result = productService.createProduct(sampleProductDto);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldUpdateAndReturnProduct() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto updatedDto = ProductDto.builder()
                .name("Updated Name")
                .price(new BigDecimal("150.00"))
                .build();

        ProductDto result = productService.updateProduct("p1", updatedDto);

        assertNotNull(result);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        when(productRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.updateProduct("nonexistent", sampleProductDto));
    }

    @Test
    void deleteProduct_ShouldCallRepository() {
        doNothing().when(productRepository).deleteById("p1");

        productService.deleteProduct("p1");

        verify(productRepository, times(1)).deleteById("p1");
    }
}
