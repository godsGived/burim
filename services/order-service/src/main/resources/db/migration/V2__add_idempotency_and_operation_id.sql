ALTER TABLE orders
    ADD COLUMN idempotency_key VARCHAR(255),
    ADD COLUMN operation_id UUID;

ALTER TABLE orders
    ADD CONSTRAINT uk_orders_user_idempotency_key
        UNIQUE (user_id, idempotency_key);

CREATE INDEX idx_orders_operation_id ON orders (operation_id);
