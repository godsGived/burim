ALTER TABLE products
ADD COLUMN IF NOT EXISTS rating NUMERIC(3, 2) NOT NULL DEFAULT 0.0,
ADD COLUMN IF NOT EXISTS reviews_count INT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS product_review_state (
    review_id BIGINT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    rating INT,
    last_version BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_review_state_product_id
ON product_review_state(product_id);