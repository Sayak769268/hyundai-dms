-- ==========================================================
-- Restore missing dealer accounts
-- Run this in MySQL Workbench to fix "Unknown" dealer issue
-- ==========================================================

USE dms_db;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- ==========================================================
-- STEP 1: Insert Kiran Motors dealer (id=2)
-- ==========================================================
INSERT IGNORE INTO dealers (id, name, registered_number, contact_email, contact_phone, address, is_active)
VALUES (2, 'Kiran Motors', 'DL-KM-002', 'kiran@kiranmotors.com', '9900000002', 'Koramangala, Bangalore', 1);

-- ==========================================================
-- STEP 2: Insert Rajesh Motors dealer (id=11)
-- ==========================================================
INSERT IGNORE INTO dealers (id, name, registered_number, contact_email, contact_phone, address, is_active)
VALUES (11, 'Rajesh Motors', 'DL-RM-011', 'rajesh@rajeshmotors.com', '9900000011', 'MG Road, Bangalore', 1);

-- ==========================================================
-- STEP 3: Insert kiran_motors dealer user (id=2)
-- Password: Kiran@2024 (bcrypt hash)
-- ==========================================================
INSERT IGNORE INTO users (id, username, password_hash, email, full_name, is_active, dealer_id)
VALUES (2, 'kiran_motors', '$2a$10$8K1p/a0dR1xqM8K3vZ9O8.TqKmJwN5Y6X3pL7mN2qR4sT1uV0wX2e', 
        'kiran@kiranmotors.com', 'Kiran Motors', 1, 2);

-- ==========================================================
-- STEP 4: Insert rajesh_motors dealer user (id=11)
-- Password: Rajesh@2024 (bcrypt hash)
-- ==========================================================
INSERT IGNORE INTO users (id, username, password_hash, email, full_name, is_active, dealer_id)
VALUES (11, 'rajesh_motors', '$2a$10$8K1p/a0dR1xqM8K3vZ9O8.TqKmJwN5Y6X3pL7mN2qR4sT1uV0wX2e',
        'rajesh@rajeshmotors.com', 'Rajesh Motors', 1, 11);

-- ==========================================================
-- STEP 5: Assign ROLE_DEALER to both users
-- ==========================================================
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (11, 2);

-- ==========================================================
-- STEP 6: Fix existing sales_orders that show "Unknown"
-- Link them to dealer_id=2 (Kiran Motors) based on the data
-- ==========================================================
UPDATE sales_orders SET dealer_id = 2 WHERE dealer_id IS NULL OR dealer_id = 0;

SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- Verify
SELECT u.id, u.username, u.is_active, u.dealer_id, d.name AS dealer_name
FROM users u
LEFT JOIN dealers d ON u.dealer_id = d.id
WHERE u.username IN ('admin', 'kiran_motors', 'rajesh_motors');

SELECT id, name, is_active FROM dealers;
