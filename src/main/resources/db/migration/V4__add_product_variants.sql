-- =============================================================
-- V4__add_product_variants.sql
-- Adds product variant support: size, color, material, etc.
-- Each variant has its own SKU, stock, optional price override,
-- and a set of key-value attributes (e.g. color=Red, size=M).
-- Cart and order items are updated to track selected variant.
-- =============================================================

-- TABLE: product_variants
CREATE TABLE IF NOT EXISTS product_variants (
    id          VARCHAR(255)   PRIMARY KEY,
    product_id  VARCHAR(255)   NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku         VARCHAR(255)   UNIQUE,
    stock       INTEGER        NOT NULL DEFAULT 0,
    price       NUMERIC(19, 2),          -- NULL means use parent product price
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- TABLE: variant_attributes  (@ElementCollection on ProductVariant.attributes)
CREATE TABLE IF NOT EXISTS variant_attributes (
    variant_id  VARCHAR(255) NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    attr_key    VARCHAR(255) NOT NULL,
    attr_value  VARCHAR(255),
    PRIMARY KEY (variant_id, attr_key)
);

-- Add variant_id to cart_items
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS variant_id VARCHAR(255);

-- Drop old single-column unique constraint (product-only key)
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_user_id_product_id_key;

-- Partial unique index: one entry per (user, product) when no variant selected
CREATE UNIQUE INDEX IF NOT EXISTS cart_items_no_variant_uq
    ON cart_items (user_id, product_id)
    WHERE variant_id IS NULL;

-- Partial unique index: one entry per (user, product, variant) when variant selected
CREATE UNIQUE INDEX IF NOT EXISTS cart_items_with_variant_uq
    ON cart_items (user_id, product_id, variant_id)
    WHERE variant_id IS NOT NULL;

-- Add variant tracking to order_items (snapshot at time of purchase)
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_id    VARCHAR(255);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_label VARCHAR(255); -- e.g. "Red / M"
