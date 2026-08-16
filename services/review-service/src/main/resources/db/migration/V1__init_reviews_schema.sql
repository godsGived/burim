CREATE TABLE IF NOT EXISTS reviews (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT                   NOT NULL,
    product_id    BIGINT                   NOT NULL,
    rating        INT                      NOT NULL,
    title         VARCHAR(255)             NOT NULL,
    description   TEXT                     NOT NULL,
    advantages    TEXT,
    disadvantages TEXT,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT uk_reviews_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product_id ON reviews (product_id);

CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews (user_id);