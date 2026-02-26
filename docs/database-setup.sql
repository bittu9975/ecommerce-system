-- Create database for Auth Service
CREATE DATABASE ecommerce_auth;

-- Connect to the database
\c ecommerce_auth;

-- The tables will be created automatically by Hibernate
-- This script is just to create the database

-- If you want to create manually:
-- CREATE TABLE users (
--     id BIGSERIAL PRIMARY KEY,
--     name VARCHAR(50) NOT NULL,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     role VARCHAR(20) NOT NULL,
--     created_at TIMESTAMP NOT NULL
-- );
