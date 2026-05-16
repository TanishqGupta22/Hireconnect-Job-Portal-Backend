-- HireConnect Database Setup
-- Create database and users table for local development

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS hireconnect 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE hireconnect;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CANDIDATE', 'RECRUITER', 'ADMIN') NOT NULL,
    provider ENUM('LOCAL', 'GOOGLE', 'GITHUB') NOT NULL DEFAULT 'LOCAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry TIMESTAMP NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255) NULL,
    email_verification_expiry TIMESTAMP NULL,
    
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_provider (provider),
    INDEX idx_created_at (created_at),
    INDEX idx_reset_token (reset_token),
    INDEX idx_email_verification_token (email_verification_token)
);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date)
);

-- Insert sample admin user (password: admin123)
INSERT IGNORE INTO users (name, email, password, role, provider) 
VALUES (
    'Admin User',
    'admin@hireconnect.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', // password: admin123
    'ADMIN',
    'LOCAL'
);

-- Show table structure
DESCRIBE users;
DESCRIBE refresh_tokens;
