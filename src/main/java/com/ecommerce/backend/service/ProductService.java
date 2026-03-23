package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.dto.ProductVariantDto;
import com.ecommerce.backend.dto.ReviewDto;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductVariant;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductVariantRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    // ------------------------------------------------------------------
    // Product CRUD
    // ------------------------------------------------------------------

    public ProductListResponse getAllProducts(String query, Category category,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Specification<Product> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ProductListResponse.builder()
                .products(productDtos)
                .totalCount(productPage.getTotalElements())
                .build();
    }

    public ProductDto getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return convertToDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = convertToEntity(dto);
        product.setId(null);
        return convertToDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(String id, ProductDto dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        existing.setName(dto.getName());
        existing.setPrice(dto.getPrice());
        existing.setImages(dto.getImages());
        existing.setCategory(dto.getCategory());
        existing.setStock(dto.getStock());
        existing.setDescription(dto.getDescription());
        existing.setSpecs(dto.getSpecs());
        existing.setFeatured(dto.getFeatured());
        return convertToDto(productRepository.save(existing));
    }

    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    // ------------------------------------------------------------------
    // Variant CRUD
    // ------------------------------------------------------------------

    public List<ProductVariantDto> getVariants(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return variantRepository.findByProductIdOrderByCreatedAtAsc(productId).stream()
                .map(this::convertVariantToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductVariantDto addVariant(String productId, ProductVariantDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (dto.getSku() != null && !dto.getSku().isBlank()
                && variantRepository.existsBySku(dto.getSku())) {
            throw new BusinessException("SKU already exists: " + dto.getSku());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(dto.getSku())
                .stock(dto.getStock())
                .price(dto.getPrice())
                .attributes(dto.getAttributes() != null ? dto.getAttributes() : new HashMap<>())
                .build();

        return convertVariantToDto(variantRepository.save(variant));
    }

    @Transactional
    public ProductVariantDto updateVariant(String productId, String variantId, ProductVariantDto dto) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));

        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException("Variant does not belong to product: " + productId);
        }
        if (dto.getSku() != null && !dto.getSku().isBlank()
                && variantRepository.existsBySkuAndIdNot(dto.getSku(), variantId)) {
            throw new BusinessException("SKU already exists: " + dto.getSku());
        }

        variant.setSku(dto.getSku());
        variant.setStock(dto.getStock());
        variant.setPrice(dto.getPrice());
        if (dto.getAttributes() != null) {
            variant.getAttributes().clear();
            variant.getAttributes().putAll(dto.getAttributes());
        }
        return convertVariantToDto(variantRepository.save(variant));
    }

    @Transactional
    public void deleteVariant(String productId, String variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));
        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException("Variant does not belong to product: " + productId);
        }
        variantRepository.deleteById(variantId);
    }

    // ------------------------------------------------------------------
    // Shared helpers (package-visible for use by CartService / OrderService)
    // ------------------------------------------------------------------

    /**
     * Builds a human-readable label from variant attributes.
     * Example: {color="Red", size="M"} → "Red / M"
     */
    public static String buildVariantLabel(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) return "";
        return attributes.values().stream()
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.joining(" / "));
    }

    /**
     * Returns the variant's price when set, otherwise the parent product's price.
     */
    public static BigDecimal resolvePrice(BigDecimal variantPrice, BigDecimal productPrice) {
        return (variantPrice != null) ? variantPrice : productPrice;
    }

    // ------------------------------------------------------------------
    // DTO conversion
    // ------------------------------------------------------------------

    public ProductDto convertToDto(Product product) {
        List<ProductVariantDto> variantDtos = (product.getVariants() == null)
                ? new ArrayList<>()
                : product.getVariants().stream()
                        .map(this::convertVariantToDto)
                        .collect(Collectors.toList());

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .images(product.getImages())
                .category(product.getCategory())
                .stock(product.getStock())
                .rating(product.getRating())
                .ratingCount(product.getRatingCount())
                .description(product.getDescription())
                .specs(product.getSpecs())
                .featured(product.getFeatured())
                .variants(variantDtos)
                .reviews(product.getReviews() == null ? new ArrayList<>()
                        : product.getReviews().stream()
                                .map(r -> ReviewDto.builder()
                                        .id(r.getId())
                                        .userName(r.getUserName())
                                        .rating(r.getRating())
                                        .title(r.getTitle())
                                        .body(r.getBody())
                                        .createdAt(r.getCreatedAt())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }

    public ProductVariantDto convertVariantToDto(ProductVariant variant) {
        return ProductVariantDto.builder()
                .id(variant.getId())
                .sku(variant.getSku())
                .stock(variant.getStock())
                .price(variant.getPrice())
                .attributes(variant.getAttributes())
                .label(buildVariantLabel(variant.getAttributes()))
                .build();
    }

    private Product convertToEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .images(dto.getImages() != null ? dto.getImages() : new ArrayList<>())
                .category(dto.getCategory())
                .stock(dto.getStock())
                .description(dto.getDescription())
                .specs(dto.getSpecs())
                .featured(dto.getFeatured())
                .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .reviews(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();
    }
}
