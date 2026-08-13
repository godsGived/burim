CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100),
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO products (name, description, price, category, brand, stock)
VALUES
('Wireless Gaming Mouse', 'Ergonomic wireless optical mouse with custom RGB lighting and 26K DPI sensor', 59.99, 'Electronics', 'Logitech', 120),
('Mechanical Keyboard K2', '75% Layout Bluetooth wireless mechanical keyboard with Gateron Brown switches', 99.90, 'Electronics', 'Keychron', 45),
('UltraWide Monitor 34"', 'Curved IPS display with 144Hz refresh rate and USB-C hub', 499.00, 'Electronics', 'LG', 15),
('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 38.50, 'Books', 'Pearson', 200),
('Designing Data-Intensive Applications', 'The big ideas behind reliable, scalable, and maintainable systems', 45.00, 'Books', 'OReilly', 150),
('Espresso Coffee Maker', '15-bar pump pressure espresso machine with milk frother', 129.99, 'Home & Kitchen', 'DeLonghi', 30);