ALTER TABLE stock_reservations
    ADD CONSTRAINT uk_stock_reservations_operation_product
        UNIQUE (operation_id, product_id);
