package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.dto.ProductVariantDto;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductVariant;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductVariantRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;
    private ProductDto sampleProductDto;
    private ProductVariant sampleVariant;

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

        sampleVariant = ProductVariant.builder()
                .id("v1")
                .product(sampleProduct)
                .sku("P1-RED-M")
                .stock(25)
                .price(new BigDecimal("120.00"))
                // Use a mutable HashMap so service's attributes.clear() / putAll() can mutate it
                .attributes(new HashMap<>(Map.of("color", "Red", "size", "M")))
                .build();
    }

    // ------------------------------------------------------------------
    // Existing product CRUD tests
    // ------------------------------------------------------------------

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

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById("nonexistent"));
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

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct("nonexistent", sampleProductDto));
    }

    @Test
    void deleteProduct_ShouldCallRepository() {
        when(productRepository.existsById("p1")).thenReturn(true);
        doNothing().when(productRepository).deleteById("p1");

        productService.deleteProduct("p1");

        verify(productRepository, times(1)).deleteById("p1");
    }

    // ------------------------------------------------------------------
    // Variant CRUD tests
    // ------------------------------------------------------------------

    @Test
    void getVariants_WhenProductExists_ReturnsVariantList() {
        when(productRepository.existsById("p1")).thenReturn(true);
        when(variantRepository.findByProductIdOrderByCreatedAtAsc("p1"))
                .thenReturn(Arrays.asList(sampleVariant));

        List<ProductVariantDto> result = productService.getVariants("p1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("v1", result.get(0).getId());
        assertEquals("P1-RED-M", result.get(0).getSku());
        assertEquals(25, result.get(0).getStock());
    }

    @Test
    void getVariants_WhenProductNotFound_ThrowsResourceNotFoundException() {
        when(productRepository.existsById("nonexistent")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getVariants("nonexistent"));
        verify(variantRepository, never()).findByProductIdOrderByCreatedAtAsc(anyString());
    }

    @Test
    void addVariant_WithUniqueSku_CreatesAndReturnsVariant() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.existsBySku("P1-RED-M")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(sampleVariant);

        ProductVariantDto dto = ProductVariantDto.builder()
                .sku("P1-RED-M")
                .stock(25)
                .price(new BigDecimal("120.00"))
                .attributes(Map.of("color", "Red", "size", "M"))
                .build();

        ProductVariantDto result = productService.addVariant("p1", dto);

        assertNotNull(result);
        assertEquals("v1", result.getId());
        assertEquals("P1-RED-M", result.getSku());
        verify(variantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void addVariant_WithDuplicateSku_ThrowsBusinessException() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.existsBySku("P1-RED-M")).thenReturn(true);

        ProductVariantDto dto = ProductVariantDto.builder()
                .sku("P1-RED-M")
                .stock(5)
                .build();

        assertThrows(BusinessException.class,
                () -> productService.addVariant("p1", dto));
        verify(variantRepository, never()).save(any());
    }

    @Test
    void addVariant_WithNullSku_SkipsSkuDuplicateCheck() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        ProductVariant noSkuVariant = ProductVariant.builder()
                .id("v2").product(sampleProduct).stock(10).build();
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(noSkuVariant);

        ProductVariantDto dto = ProductVariantDto.builder()
                .sku(null)
                .stock(10)
                .build();

        ProductVariantDto result = productService.addVariant("p1", dto);

        assertNotNull(result);
        verify(variantRepository, never()).existsBySku(anyString());
        verify(variantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void updateVariant_WhenVariantExists_UpdatesAndReturnsVariant() {
        when(variantRepository.findById("v1")).thenReturn(Optional.of(sampleVariant));
        when(variantRepository.existsBySkuAndIdNot("P1-BLUE-L", "v1")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(sampleVariant);

        ProductVariantDto updateDto = ProductVariantDto.builder()
                .sku("P1-BLUE-L")
                .stock(15)
                .price(new BigDecimal("130.00"))
                .attributes(Map.of("color", "Blue", "size", "L"))
                .build();

        ProductVariantDto result = productService.updateVariant("p1", "v1", updateDto);

        assertNotNull(result);
        verify(variantRepository, times(1)).save(any(ProductVariant.class));
    }

    @Test
    void updateVariant_WhenVariantBelongsToDifferentProduct_ThrowsBusinessException() {
        // Create a variant that belongs to product "p2", not "p1"
        Product otherProduct = Product.builder().id("p2").name("Other Product")
                .price(BigDecimal.TEN).stock(5).build();
        ProductVariant wrongVariant = ProductVariant.builder()
                .id("v1").product(otherProduct).stock(5).build();
        when(variantRepository.findById("v1")).thenReturn(Optional.of(wrongVariant));

        ProductVariantDto updateDto = ProductVariantDto.builder().stock(5).build();

        assertThrows(BusinessException.class,
                () -> productService.updateVariant("p1", "v1", updateDto));
        verify(variantRepository, never()).save(any());
    }

    @Test
    void deleteVariant_WhenVariantExists_DeletesVariant() {
        when(variantRepository.findById("v1")).thenReturn(Optional.of(sampleVariant));
        doNothing().when(variantRepository).deleteById("v1");

        productService.deleteVariant("p1", "v1");

        verify(variantRepository, times(1)).deleteById("v1");
    }

    @Test
    void deleteVariant_WhenVariantBelongsToDifferentProduct_ThrowsBusinessException() {
        Product otherProduct = Product.builder().id("p2").name("Other Product")
                .price(BigDecimal.TEN).stock(5).build();
        ProductVariant wrongVariant = ProductVariant.builder()
                .id("v1").product(otherProduct).stock(5).build();
        when(variantRepository.findById("v1")).thenReturn(Optional.of(wrongVariant));

        assertThrows(BusinessException.class,
                () -> productService.deleteVariant("p1", "v1"));
        verify(variantRepository, never()).deleteById(anyString());
    }

    // ------------------------------------------------------------------
    // Static helper tests
    // ------------------------------------------------------------------

    @Test
    void buildVariantLabel_WhenAttributesPresent_ReturnsJoinedValues() {
        // Map order may vary; check that both values appear
        String label = ProductService.buildVariantLabel(Map.of("color", "Red", "size", "M"));

        assertNotNull(label);
        assertTrue(label.contains("Red"));
        assertTrue(label.contains("M"));
    }

    @Test
    void buildVariantLabel_WhenAttributesEmpty_ReturnsEmptyString() {
        String label = ProductService.buildVariantLabel(Map.of());

        assertEquals("", label);
    }

    @Test
    void buildVariantLabel_WhenAttributesNull_ReturnsEmptyString() {
        String label = ProductService.buildVariantLabel(null);

        assertEquals("", label);
    }

    @Test
    void resolvePrice_WhenVariantPricePresent_ReturnsVariantPrice() {
        BigDecimal variantPrice = new BigDecimal("120.00");
        BigDecimal productPrice = new BigDecimal("100.00");

        BigDecimal result = ProductService.resolvePrice(variantPrice, productPrice);

        assertEquals(variantPrice, result);
    }

    @Test
    void resolvePrice_WhenVariantPriceNull_ReturnsProductPrice() {
        BigDecimal productPrice = new BigDecimal("100.00");

        BigDecimal result = ProductService.resolvePrice(null, productPrice);

        assertEquals(productPrice, result);
    }

    // ------------------------------------------------------------------
    // convertToDto includes variants
    // ------------------------------------------------------------------

    @Test
    void getProductById_WhenProductHasVariants_DtoIncludesVariants() {
        Product productWithVariant = Product.builder()
                .id("p1")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .category(Category.Electronics)
                .stock(10)
                .images(new ArrayList<>())
                .reviews(new ArrayList<>())
                .variants(Arrays.asList(sampleVariant))
                .build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(productWithVariant));

        ProductDto result = productService.getProductById("p1");

        assertNotNull(result.getVariants());
        assertEquals(1, result.getVariants().size());
        assertEquals("v1", result.getVariants().get(0).getId());
        assertEquals("P1-RED-M", result.getVariants().get(0).getSku());
        // Label should be computed from attributes
        assertNotNull(result.getVariants().get(0).getLabel());
        assertFalse(result.getVariants().get(0).getLabel().isBlank());
    }
}
