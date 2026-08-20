CREATE TABLE stock_reservations (
    id BIGSERIAL PRIMARY KEY,
    operation_id UUID NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_reservations_op_id ON stock_reservations(operation_id);