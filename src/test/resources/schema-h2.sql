-- H2 Test Database Schema
-- This is a simplified schema for testing purposes

-- Merchant table
CREATE TABLE IF NOT EXISTS merchant (
    merchant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    shop_name VARCHAR(100),
    shop_avatar VARCHAR(255),
    shop_intro VARCHAR(500),
    pickup_location VARCHAR(255),
    qualification_url VARCHAR(255),
    audit_status INT DEFAULT 0,
    status INT DEFAULT 1,
    ban_reason VARCHAR(255),
    ban_end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- Student table
CREATE TABLE IF NOT EXISTS student (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    school_id VARCHAR(50),
    avatar VARCHAR(255),
    nickname VARCHAR(50),
    balance DECIMAL(10,2) DEFAULT 0.00,
    address_list TEXT,
    status INT DEFAULT 1,
    ban_reason VARCHAR(255),
    ban_end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- Product table
CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    specs VARCHAR(500),
    original_price DECIMAL(10,2),
    proxy_price DECIMAL(10,2),
    stock INT DEFAULT 0,
    channel VARCHAR(100),
    description VARCHAR(500),
    images TEXT,
    audit_status INT DEFAULT 0,
    status INT DEFAULT 1,
    source_request_id BIGINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- Favorite table
CREATE TABLE IF NOT EXISTS favorite (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT unique_favorite UNIQUE (student_id, product_id, deleted)
);

-- Insert test merchant (required for products)
MERGE INTO merchant (merchant_id, username, password, shop_name, status, deleted) 
KEY (merchant_id) 
VALUES (1, 'test_merchant', 'password', 'Test Shop', 1, 0);