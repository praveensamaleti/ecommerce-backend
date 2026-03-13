package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.ProductListResponse;
import com.ecommerce.backend.dto.ReviewDto;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.repository.ProductRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductListResponse getAllProducts(String query, Category category, BigDecimal minPrice, BigDecimal maxPrice, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        
        Specification<Product> spec = (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (query != null && !query.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.trim().toLowerCase() + "%"));
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
            
            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
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
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = convertToEntity(productDto);
        product.setId(null); // Ensure ID is generated
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(String id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        existingProduct.setName(productDto.getName());
        existingProduct.setPrice(productDto.getPrice());
        existingProduct.setImages(productDto.getImages());
        existingProduct.setCategory(productDto.getCategory());
        existingProduct.setStock(productDto.getStock());
        existingProduct.setDescription(productDto.getDescription());
        existingProduct.setSpecs(productDto.getSpecs());
        existingProduct.setFeatured(productDto.getFeatured());
        
        Product savedProduct = productRepository.save(existingProduct);
        return convertToDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    private ProductDto convertToDto(Product product) {
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
                .reviews(product.getReviews().stream()
                        .map(review -> ReviewDto.builder()
                                .id(review.getId())
                                .userName(review.getUserName())
                                .rating(review.getRating())
                                .title(review.getTitle())
                                .body(review.getBody())
                                .createdAt(review.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Product convertToEntity(ProductDto dto) {
        return Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .images(dto.getImages())
                .category(dto.getCategory())
                .stock(dto.getStock())
                .description(dto.getDescription())
                .specs(dto.getSpecs())
                .featured(dto.getFeatured())
                .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                .ratingCount(dto.getRatingCount() != null ? dto.getRatingCount() : 0)
                .reviews(new ArrayList<>())
                .build();
    }
}
