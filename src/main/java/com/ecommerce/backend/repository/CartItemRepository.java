package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByUserId(String userId);

    // ------------------------------------------------------------------
    // Variant-aware lookups
    // Use these instead of the plain productId-only methods wherever
    // the user might have items with variants in their cart.
    // ------------------------------------------------------------------

    /** Finds a cart item for a product that has NO variant selected. */
    Optional<CartItem> findByUserIdAndProductIdAndVariantIdIsNull(String userId, String productId);

    /** Finds a cart item for a specific product+variant combination. */
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(String userId, String productId, String variantId);

    // ------------------------------------------------------------------
    // Legacy / backward-compatible (products with no variant support)
    // ------------------------------------------------------------------

    /** @deprecated Prefer the variant-aware overloads. */
    @Deprecated
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    // ------------------------------------------------------------------
    // Deletes
    // ------------------------------------------------------------------

    /** Removes the no-variant entry for a product from a user's cart. */
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.userId = :userId AND c.productId = :productId AND c.variantId IS NULL")
    void deleteByUserIdAndProductIdAndVariantIdIsNull(@Param("userId") String userId,
                                                       @Param("productId") String productId);

    /** Removes a specific product+variant entry from a user's cart. */
    void deleteByUserIdAndProductIdAndVariantId(String userId, String productId, String variantId);

    /** @deprecated Prefer the variant-aware overloads. */
    @Deprecated
    void deleteByUserIdAndProductId(String userId, String productId);

    /** Clears the entire cart for a user (all products and variants). */
    void deleteByUserId(String userId);
}
