package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    /** Returns all variants belonging to a product, ordered by creation time. */
    List<ProductVariant> findByProductIdOrderByCreatedAtAsc(String productId);

    /** Looks up a variant by its unique SKU. */
    Optional<ProductVariant> findBySku(String sku);

    /** Checks whether a SKU already exists (for duplicate-SKU validation). */
    boolean existsBySku(String sku);

    /** Checks if a SKU exists but belongs to a different variant (for update validation). */
    boolean existsBySkuAndIdNot(String sku, String id);
}
