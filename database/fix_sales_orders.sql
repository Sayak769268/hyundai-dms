-- ==========================================================
-- Fix sales_orders table - make ALL legacy columns nullable
-- Run this in MySQL Workbench ONCE
-- ==========================================================

USE dms_db;
SET FOREIGN_KEY_CHECKS = 0;

-- Drop all foreign keys on sales_orders first
ALTER TABLE sales_orders DROP FOREIGN KEY IF EXISTS sales_orders_ibfk_1;
ALTER TABLE sales_orders DROP FOREIGN KEY IF EXISTS sales_orders_ibfk_2;
ALTER TABLE sales_orders DROP FOREIGN KEY IF EXISTS sales_orders_ibfk_3;

-- Make ALL legacy NOT NULL columns nullable
ALTER TABLE sales_orders MODIFY COLUMN lead_id BIGINT NULL;
ALTER TABLE sales_orders MODIFY COLUMN inventory_id BIGINT NULL;
ALTER TABLE sales_orders MODIFY COLUMN employee_id BIGINT NULL;
ALTER TABLE sales_orders MODIFY COLUMN total_amount DECIMAL(15,2) NULL;
ALTER TABLE sales_orders MODIFY COLUMN booking_amount DECIMAL(15,2) NULL;

SET FOREIGN_KEY_CHECKS = 1;

-- Verify
DESCRIBE sales_orders;
