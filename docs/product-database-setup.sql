-- Create database for Product Service
CREATE DATABASE ecommerce_product;

-- Connect to the database
\c ecommerce_product;

-- Tables will be created automatically by Hibernate
-- This script is just to create the database

-- Optional: Create tables manually if needed
-- CREATE TABLE categories (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(255) NOT NULL UNIQUE,
--     description VARCHAR(500),
--     created_at TIMESTAMP NOT NULL,
--     updated_at TIMESTAMP
-- );

-- CREATE TABLE products (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     description VARCHAR(2000),
--     price DECIMAL(10,2) NOT NULL,
--     stock INTEGER NOT NULL,
--     image_url VARCHAR(255),
--     category_id BIGINT NOT NULL,
--     active BOOLEAN DEFAULT TRUE,
--     brand VARCHAR(255),
--     sku VARCHAR(100),
--     created_at TIMESTAMP NOT NULL,
--     updated_at TIMESTAMP,
--     FOREIGN KEY (category_id) REFERENCES categories(id)
-- );

-- Sample data for testing
-- INSERT INTO categories (name, description, created_at, updated_at) VALUES
-- ('Electronics', 'Electronic devices and gadgets', NOW(), NOW()),
-- ('Books', 'Books and publications', NOW(), NOW()),
-- ('Clothing', 'Apparel and accessories', NOW(), NOW());
