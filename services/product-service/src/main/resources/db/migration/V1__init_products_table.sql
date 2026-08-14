CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id)
);

INSERT INTO categories (name, description) VALUES
('Electronics', 'Gadgets and electronic devices'),
('Books', 'Printed and electronic books'),
('Home & Kitchen', 'Appliances and home equipment');

INSERT INTO brands (name) VALUES
('Logitech'),
('Keychron'),
('LG'),
('Pearson'),
('OReilly'),
('DeLonghi');

INSERT INTO products (name, description, price, category_id, brand_id, stock) VALUES
('Wireless Gaming Mouse', 'Ergonomic wireless optical mouse with custom RGB lighting and 26K DPI sensor', 59.99, 1, 1, 120),
('Mechanical Keyboard K2', '75% Layout Bluetooth wireless mechanical keyboard with Gateron Brown switches', 99.90, 1, 2, 45),
('UltraWide Monitor 34"', 'Curved IPS display with 144Hz refresh rate and USB-C hub', 499.00, 1, 3, 15),
('Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 38.50, 2, 4, 200),
('Designing Data-Intensive Applications', 'The big ideas behind reliable, scalable, and maintainable systems', 45.00, 2, 5, 150),
('Espresso Coffee Maker', '15-bar pump pressure espresso machine with milk frother', 129.99, 3, 6, 30);