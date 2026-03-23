-- =============================================================
-- V2__example_add_column.sql
-- EXAMPLE: Adding a new column to an existing table safely.
--
-- HOW TO NAME YOUR FILES:
--   V{version}__{description}.sql
--   e.g. V2__add_coupon_code_to_orders.sql
--        V3__create_categories_table.sql
--        V4__drop_legacy_column.sql
--
-- RULES:
--   1. NEVER edit a migration file after it has been applied.
--   2. NEVER delete a migration file.
--   3. Each version number must be unique and always increasing.
--   4. Keep migrations small and focused — one change per file.
--   5. Always test on a copy of prod data before applying.
--   6. For destructive changes (DROP, TRUNCATE), add a comment explaining why.
-- =============================================================

-- Example: safely add a nullable column (no data loss risk)
-- ALTER TABLE orders ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(50);

-- Example: add a new index for query performance
-- CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);

-- Example: add a new table
-- CREATE TABLE IF NOT EXISTS coupons (
--     id          VARCHAR(255) PRIMARY KEY,
--     code        VARCHAR(50)  NOT NULL UNIQUE,
--     discount    NUMERIC(5,2) NOT NULL,
--     expires_at  TIMESTAMP
-- );

