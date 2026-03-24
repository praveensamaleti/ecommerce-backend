-- =============================================================
-- V5__add_featured_to_products.sql
-- Adds the featured flag to the products table.
-- Defaults existing rows to false.
-- =============================================================

ALTER TABLE products ADD COLUMN IF NOT EXISTS featured BOOLEAN NOT NULL DEFAULT FALSE;
