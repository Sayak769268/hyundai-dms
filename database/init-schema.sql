-- ==========================================================
-- Dealer Management System (DMS) MySQL Schema
-- Run this entire script in MySQL Workbench to initialize.
-- ==========================================================

CREATE DATABASE IF NOT EXISTS dms_db;
USE dms_db;

SET FOREIGN_KEY_CHECKS = 0;
-- We will drop all tables if they exist to allow easy resets
DROP TABLE IF EXISTS audit_logs, role_menus, menus, payments, accessories, exchanges, test_drives, sales_orders, inventory, vehicle_variants, vehicles, follow_ups, customer_leads, customers, employees, departments, branches, dealers, user_roles, role_permissions, permissions, roles, login_history, users;
SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- MODULE 1: User & Role Management & Auth
-- ==========================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, -- e.g., ROLE_ADMIN, ROLE_DEALER, ROLE_EMPLOYEE
    description VARCHAR(255)
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE -- e.g., VIEW_INVENTORY, CREATE_CUSTOMER
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20), -- SUCCESS, FAILED
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ==========================================================
-- MODULE 2: Dealer & Organization Management
-- ==========================================================

CREATE TABLE dealers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    registered_number VARCHAR(100) UNIQUE,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(20),
    address TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dealer_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(255),
    FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE
);

CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL, -- Sales, Service, Finance
    FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

-- ==========================================================
-- MODULE 9: Employee Management
-- ==========================================================

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT,
    employee_code VARCHAR(50) UNIQUE NOT NULL,
    designation VARCHAR(100),
    hire_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

-- ==========================================================
-- MODULE 3: Customer (CRM)
-- ==========================================================

CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    dealer_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dealer_id) REFERENCES dealers(id) ON DELETE CASCADE
);

CREATE TABLE customer_leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    source VARCHAR(50), -- Walk-in, Website, Referral
    status VARCHAR(50) DEFAULT 'NEW', -- NEW, CONTACTED, QUALIFIED, LOST, CONVERTED
    assigned_employee_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_employee_id) REFERENCES employees(id) ON DELETE SET NULL
);

CREATE TABLE follow_ups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    follow_up_date DATETIME NOT NULL,
    notes TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES customer_leads(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT
);

-- ==========================================================
-- MODULE 4: Vehicle & Inventory Management
-- ==========================================================

CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    brand VARCHAR(100) DEFAULT 'Hyundai',
    year INT NOT NULL,
    base_price DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vehicle_variants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    variant_name VARCHAR(100) NOT NULL, -- e.g., S, SX, SX(O)
    fuel_type VARCHAR(50), -- Petrol, Diesel, EV
    transmission VARCHAR(50), -- MT, AT, DCT
    additional_cost DECIMAL(15,2) DEFAULT 0,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    variant_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    vin VARCHAR(20) UNIQUE NOT NULL,
    engine_number VARCHAR(50) UNIQUE,
    color VARCHAR(50),
    status VARCHAR(50) DEFAULT 'AVAILABLE', -- AVAILABLE, RESERVED, SOLD, IN_TRANSIT
    arrival_date DATE,
    FOREIGN KEY (variant_id) REFERENCES vehicle_variants(id),
    FOREIGN KEY (dealer_id) REFERENCES dealers(id)
);

-- ==========================================================
-- MODULE 5: Sales & Booking System
-- ==========================================================

CREATE TABLE sales_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL UNIQUE, -- One car per order
    employee_id BIGINT NOT NULL,
    booking_amount DECIMAL(15,2),
    total_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'BOOKED', -- BOOKED, INVOICED, CANCELLED, DELIVERED
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lead_id) REFERENCES customer_leads(id),
    FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ==========================================================
-- MODULE 6: Test Drive Management
-- ==========================================================

CREATE TABLE test_drives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    scheduled_date DATETIME NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED', -- SCHEDULED, COMPLETED, CANCELLED
    feedback TEXT,
    FOREIGN KEY (lead_id) REFERENCES customer_leads(id),
    FOREIGN KEY (variant_id) REFERENCES vehicle_variants(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ==========================================================
-- MODULE 7: Exchange Management
-- ==========================================================

CREATE TABLE exchanges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lead_id BIGINT NOT NULL,
    old_vehicle_make VARCHAR(100),
    old_vehicle_model VARCHAR(100),
    year INT,
    mileage INT,
    offered_value DECIMAL(15,2),
    status VARCHAR(50) DEFAULT 'INSPECTING', -- INSPECTING, APPROVED, REJECTED
    FOREIGN KEY (lead_id) REFERENCES customer_leads(id) ON DELETE CASCADE
);

-- ==========================================================
-- MODULE 8: Accessories Management
-- ==========================================================

CREATE TABLE accessories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    part_number VARCHAR(100) UNIQUE,
    price DECIMAL(10,2) NOT NULL,
    dealer_id BIGINT NOT NULL,
    stock_quantity INT DEFAULT 0,
    FOREIGN KEY (dealer_id) REFERENCES dealers(id)
);

-- ==========================================================
-- MODULE 10: Finance & Payments
-- ==========================================================

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sales_order_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_mode VARCHAR(50), -- CASH, CARD, BANK_TRANSFER, FINANCE
    transaction_ref VARCHAR(100),
    payment_status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, COMPLETED, FAILED, REFUNDED
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sales_order_id) REFERENCES sales_orders(id) ON DELETE CASCADE
);

-- ==========================================================
-- MODULE 11: Menu Management
-- ==========================================================

CREATE TABLE menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(100),
    parent_id BIGINT NULL,
    display_order INT DEFAULT 0,
    FOREIGN KEY (parent_id) REFERENCES menus(id) ON DELETE SET NULL
);

CREATE TABLE role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
);

-- ==========================================================
-- MODULE 13: Audit Logs
-- ==========================================================

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL, -- e.g., CREATE_CUSTOMER, UPDATE_INVENTORY
    entity_type VARCHAR(100) NOT NULL, -- e.g., Customer, Inventory
    entity_id BIGINT,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ==========================================================
-- INSERT SAMPLE DATA
-- ==========================================================

-- 1. Roles & Permissions
INSERT INTO roles (id, name, description) VALUES 
(1, 'ROLE_ADMIN', 'OEM Level System Administrator'),
(2, 'ROLE_DEALER', 'Dealership Owner or Manager'),
(3, 'ROLE_EMPLOYEE', 'Sales or Service Staff');

INSERT INTO permissions (id, name) VALUES 
(1, 'VIEW_INVENTORY'), (2, 'MANAGE_INVENTORY'),
(3, 'VIEW_CUSTOMERS'), (4, 'MANAGE_CUSTOMERS'),
(5, 'PROCESS_SALES'), (6, 'MANAGE_USERS');

-- Map all permissions to admin
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(1,1), (1,2), (1,3), (1,4), (1,5), (1,6);
-- Dealer permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(2,1), (2,2), (2,3), (2,4), (2,5);
-- Employee permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(3,1), (3,3), (3,5);

-- 2. Passwords: all set to 'password123' (bcrypt hash)
-- Bcrypt hash for 'password123'
SET @default_pass = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

INSERT INTO users (id, username, password_hash, email, full_name, is_active) VALUES
(1, 'admin', @default_pass, 'admin@hyundai.com', 'Super Admin', 1),
(2, 'dealer_south', @default_pass, 'south@hyundaidealer.com', 'South Hub Dealer', 1),
(3, 'emp_john', @default_pass, 'john@hyundaidealer.com', 'John Sales', 1);

INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), (2, 2), (3, 3);

-- 3. Dealers, Branches, Depts, Employees
INSERT INTO dealers (id, name, registered_number, contact_email) VALUES
(1, 'Prime Hyundai South', 'DL-HS-123', 'manager@primehyundai.com');

INSERT INTO branches (id, dealer_id, name, location) VALUES
(1, 1, 'Main Showroom', '123 South Ave');

INSERT INTO departments (id, branch_id, name) VALUES
(1, 1, 'Sales'), (2, 1, 'Service');

INSERT INTO employees (id, user_id, department_id, employee_code, designation) VALUES
(1, 3, 1, 'EMP001', 'Senior Sales Executive');

-- 4. Vehicles & Inventory
INSERT INTO vehicles (id, model_name, year, base_price) VALUES
(1, 'Creta', 2024, 1100000.00),
(2, 'Venue', 2024, 794000.00),
(3, 'Tucson', 2024, 2900000.00);

INSERT INTO vehicle_variants (id, vehicle_id, variant_name, fuel_type, transmission, additional_cost) VALUES
(1, 1, 'SX(O)', 'Petrol', 'AT', 350000.00),
(2, 1, 'E', 'Diesel', 'MT', 100000.00),
(3, 2, 'S', 'Petrol', 'MT', 50000.00);

INSERT INTO inventory (id, variant_id, dealer_id, vin, engine_number, color, status) VALUES
(1, 1, 1, 'VIN12345678CRET1', 'ENG-CRET-01', 'Phantom Black', 'AVAILABLE'),
(2, 2, 1, 'VIN12345678CRET2', 'ENG-CRET-02', 'Polar White', 'AVAILABLE'),
(3, 3, 1, 'VIN12345678VENU1', 'ENG-VENU-01', 'Typhoon Silver', 'SOLD');

-- 5. CRM (Customers & Leads)
INSERT INTO customers (id, first_name, last_name, email, phone, address, dealer_id) VALUES
(1, 'Alice', 'Smith', 'alice@example.com', '9876543210', '101 Main St', 1),
(2, 'Bob', 'Jones', 'bob@example.com', '9876543211', '102 Oak St', 1);

INSERT INTO customer_leads (id, customer_id, source, status, assigned_employee_id) VALUES
(1, 1, 'Website', 'QUALIFIED', 1),
(2, 2, 'Walk-in', 'CONVERTED', 1);

-- 6. Sales
INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, booking_amount, total_amount, status) VALUES
(1, 2, 3, 1, 50000.00, 844000.00, 'INVOICED');

-- 7. Menus
INSERT INTO menus (id, name, path, icon, display_order) VALUES
(1, 'Dashboard', '/dashboard', 'HomeIcon', 1),
(2, 'Inventory', '/inventory', 'CarIcon', 2),
(3, 'CRM', '/crm', 'UsersIcon', 3),
(4, 'Sales', '/sales', 'ReceiptIcon', 4),
(5, 'Admin', '/admin', 'SettingsIcon', 5);

INSERT INTO role_menus (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- Admin sees all
(2, 1), (2, 2), (2, 3), (2, 4),         -- Dealer sees ops
(3, 1), (3, 2), (3, 3);                 -- Employee sees limited

COMMIT;
