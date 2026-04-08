-- ==========================================================
-- DMS Complete Schema Fix
-- Run EACH statement individually in Workbench (Ctrl+Enter)
-- SKIP any that say "Duplicate column name" - already exists
-- ==========================================================

USE dms_db;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- vehicles table
ALTER TABLE vehicles ADD COLUMN variant VARCHAR(100) NULL;
ALTER TABLE vehicles ADD COLUMN stock INT NOT NULL DEFAULT 0;
ALTER TABLE vehicles ADD COLUMN dealer_id BIGINT NULL;
ALTER TABLE vehicles ADD COLUMN updated_at DATETIME NULL;

-- customers table
ALTER TABLE customers ADD COLUMN notes TEXT NULL;
ALTER TABLE customers ADD COLUMN status VARCHAR(20) DEFAULT 'NEW';
ALTER TABLE customers ADD COLUMN assigned_employee_id BIGINT NULL;
ALTER TABLE customers ADD COLUMN next_follow_up_date DATE NULL;
ALTER TABLE customers ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- sales_orders table
ALTER TABLE sales_orders ADD COLUMN created_at DATETIME(6) NULL;
ALTER TABLE sales_orders ADD COLUMN final_amount DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE sales_orders ADD COLUMN price DECIMAL(15,2) NOT NULL DEFAULT 0;
ALTER TABLE sales_orders ADD COLUMN discount DECIMAL(15,2) NULL DEFAULT 0;
ALTER TABLE sales_orders ADD COLUMN customer_id BIGINT NULL;
ALTER TABLE sales_orders ADD COLUMN vehicle_id BIGINT NULL;
ALTER TABLE sales_orders ADD COLUMN dealer_id BIGINT NULL;

-- audit_logs table
ALTER TABLE audit_logs ADD COLUMN description TEXT NULL;

-- users table
ALTER TABLE users ADD COLUMN dealer_id BIGINT NULL;

-- employees table
ALTER TABLE employees ADD COLUMN dealer_id BIGINT NULL;

-- Backfill data
UPDATE sales_orders SET created_at = order_date WHERE created_at IS NULL AND order_date IS NOT NULL;
UPDATE sales_orders SET final_amount = total_amount WHERE final_amount = 0 AND total_amount IS NOT NULL;
UPDATE sales_orders SET price = total_amount WHERE price = 0 AND total_amount IS NOT NULL;

-- Activate all
UPDATE dealers SET is_active = 1 WHERE id > 0;
UPDATE users SET is_active = 1 WHERE id > 0;

SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Fix complete!' AS status;
