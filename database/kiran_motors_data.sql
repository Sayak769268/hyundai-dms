-- ==========================================================
-- Kiran Motors Sample Data (dealer_id = 2)
-- Dealer: kiran_motors / Kiran@2024
-- 20-30 records per section for pagination testing
-- ==========================================================

USE dms_db;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

-- Clean up partial data from previous runs
DELETE FROM payments       WHERE sales_order_id BETWEEN 501 AND 530;
DELETE FROM sales_orders   WHERE id BETWEEN 501 AND 530;
DELETE FROM customers      WHERE id BETWEEN 501 AND 530;
DELETE FROM employees      WHERE id BETWEEN 501 AND 515;
DELETE FROM user_roles     WHERE user_id BETWEEN 501 AND 515;
DELETE FROM users          WHERE id BETWEEN 501 AND 515;
DELETE FROM vehicles       WHERE id BETWEEN 501 AND 525;
DELETE FROM departments    WHERE id BETWEEN 50 AND 53;
DELETE FROM branches       WHERE id BETWEEN 50 AND 51;

-- ==========================================================
-- BRANCHES & DEPARTMENTS
-- ==========================================================
INSERT INTO branches (id, dealer_id, name, location) VALUES
(50, 2, 'Kiran Motors Main', 'Koramangala, Bangalore'),
(51, 2, 'Kiran Motors North', 'Hebbal, Bangalore');

INSERT INTO departments (id, branch_id, name) VALUES
(50, 50, 'Sales'),
(51, 50, 'Service'),
(52, 50, 'Finance'),
(53, 51, 'Sales');

-- ==========================================================
-- EMPLOYEE USERS (15 employees)
-- password: password123
-- ==========================================================
INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES
(501, 'km_arjun',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'arjun@kiran.com',    'Arjun Sharma',    1, 2),
(502, 'km_priya',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'priya@kiran.com',    'Priya Nair',      1, 2),
(503, 'km_rahul',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'rahul@kiran.com',    'Rahul Verma',     1, 2),
(504, 'km_sneha',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'sneha@kiran.com',    'Sneha Pillai',    1, 2),
(505, 'km_karthik',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'karthik@kiran.com',  'Karthik Rajan',   1, 2),
(506, 'km_divya',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'divya@kiran.com',    'Divya Menon',     1, 2),
(507, 'km_vikram',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'vikram@kiran.com',   'Vikram Singh',    1, 2),
(508, 'km_ananya',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ananya@kiran.com',   'Ananya Iyer',     1, 2),
(509, 'km_suresh',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'suresh@kiran.com',   'Suresh Kumar',    1, 2),
(510, 'km_meera',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'meera@kiran.com',    'Meera Krishnan',  1, 2),
(511, 'km_aditya',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'aditya@kiran.com',   'Aditya Patel',    1, 2),
(512, 'km_lakshmi',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'lakshmi@kiran.com',  'Lakshmi Devi',    1, 2),
(513, 'km_rohan',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'rohan@kiran.com',    'Rohan Mehta',     1, 2),
(514, 'km_pooja',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'pooja@kiran.com',    'Pooja Sharma',    1, 2),
(515, 'km_nikhil',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'nikhil@kiran.com',   'Nikhil Chandra',  1, 2);

INSERT INTO user_roles (user_id, role_id) VALUES
(501,3),(502,3),(503,3),(504,3),(505,3),(506,3),(507,3),(508,3),
(509,3),(510,3),(511,3),(512,3),(513,3),(514,3),(515,3);

INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES
(501, 501, 50, 'EMP-2-501', 'Senior Sales Executive', '2021-06-01', 2),
(502, 502, 50, 'EMP-2-502', 'Sales Executive',        '2022-01-15', 2),
(503, 503, 50, 'EMP-2-503', 'Sales Executive',        '2022-04-10', 2),
(504, 504, 50, 'EMP-2-504', 'Sales Consultant',       '2022-08-20', 2),
(505, 505, 53, 'EMP-2-505', 'Senior Sales Executive', '2021-03-05', 2),
(506, 506, 51, 'EMP-2-506', 'Service Advisor',        '2022-11-12', 2),
(507, 507, 51, 'EMP-2-507', 'Service Technician',     '2023-01-28', 2),
(508, 508, 53, 'EMP-2-508', 'Sales Consultant',       '2023-03-15', 2),
(509, 509, 52, 'EMP-2-509', 'Finance Manager',        '2020-09-01', 2),
(510, 510, 50, 'EMP-2-510', 'Sales Executive',        '2023-07-08', 2),
(511, 511, 53, 'EMP-2-511', 'Sales Executive',        '2023-09-14', 2),
(512, 512, 52, 'EMP-2-512', 'Finance Executive',      '2022-12-01', 2),
(513, 513, 50, 'EMP-2-513', 'Sales Executive',        '2024-01-10', 2),
(514, 514, 53, 'EMP-2-514', 'Sales Consultant',       '2024-02-20', 2),
(515, 515, 50, 'EMP-2-515', 'Junior Sales Executive', '2024-03-05', 2);

-- ==========================================================
-- VEHICLES (25 vehicles, dealer_id = 2)
-- ==========================================================
INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES
(501, 'Creta',  'Hyundai', 'E Petrol MT',        2024, 1099000.00, 7,  2),
(502, 'Creta',  'Hyundai', 'S Petrol MT',        2024, 1199000.00, 5,  2),
(503, 'Creta',  'Hyundai', 'SX Petrol AT',       2024, 1499000.00, 4,  2),
(504, 'Creta',  'Hyundai', 'SX(O) Diesel AT',    2024, 1799000.00, 2,  2),
(505, 'Venue',  'Hyundai', 'E Petrol MT',        2024,  794000.00, 9,  2),
(506, 'Venue',  'Hyundai', 'S Petrol MT',        2024,  894000.00, 6,  2),
(507, 'Venue',  'Hyundai', 'SX Turbo DCT',       2024, 1094000.00, 3,  2),
(508, 'Verna',  'Hyundai', 'EX Petrol MT',       2024, 1099000.00, 5,  2),
(509, 'Verna',  'Hyundai', 'S Petrol IVT',       2024, 1299000.00, 4,  2),
(510, 'Verna',  'Hyundai', 'SX Turbo DCT',       2024, 1499000.00, 2,  2),
(511, 'i20',    'Hyundai', 'Era Petrol MT',       2024,  699000.00, 8,  2),
(512, 'i20',    'Hyundai', 'Magna Petrol MT',     2024,  799000.00, 6,  2),
(513, 'i20',    'Hyundai', 'Sportz Petrol IVT',   2024,  949000.00, 4,  2),
(514, 'i20',    'Hyundai', 'Asta Turbo DCT',      2024, 1099000.00, 2,  2),
(515, 'Tucson', 'Hyundai', 'Platinum Petrol AT',  2024, 2799000.00, 2,  2),
(516, 'Tucson', 'Hyundai', 'Signature Diesel AT', 2024, 2999000.00, 1,  2),
(517, 'Exter',  'Hyundai', 'EX Petrol MT',        2024,  599000.00, 11, 2),
(518, 'Exter',  'Hyundai', 'S Petrol MT',         2024,  699000.00, 7,  2),
(519, 'Exter',  'Hyundai', 'SX Petrol AMT',       2024,  799000.00, 4,  2),
(520, 'Exter',  'Hyundai', 'SX(O) CNG MT',        2024,  849000.00, 3,  2),
(521, 'Alcazar','Hyundai', 'Platinum Petrol AT',  2024, 2099000.00, 2,  2),
(522, 'Alcazar','Hyundai', 'Signature Diesel AT', 2024, 2299000.00, 1,  2),
(523, 'Aura',   'Hyundai', 'S Petrol MT',         2024,  799000.00, 5,  2),
(524, 'Aura',   'Hyundai', 'SX Petrol AMT',       2024,  899000.00, 3,  2),
(525, 'Ioniq 5','Hyundai', 'Standard Range',      2024, 4499000.00, 1,  2);

-- ==========================================================
-- CUSTOMERS (30 customers, dealer_id = 2)
-- ==========================================================
INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES
(501, 'Aarav',    'Mehta',      'aarav.km@gmail.com',      '9880001001', '12 Koramangala, Bangalore',     'Interested in Creta SX',        'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 170 DAY)),
(502, 'Bhavna',   'Sharma',     'bhavna.km@gmail.com',     '9880001002', '34 Indiranagar, Bangalore',     'Looking for family SUV',        'NEW',        502, 2, DATE_SUB(NOW(), INTERVAL 165 DAY)),
(503, 'Chetan',   'Patel',      'chetan.km@gmail.com',     '9880001003', '56 HSR Layout, Bangalore',      'Test drive done for Venue',     'BOOKED',     503, 2, DATE_SUB(NOW(), INTERVAL 160 DAY)),
(504, 'Deepika',  'Nair',       'deepika.km@gmail.com',    '9880001004', '78 Jayanagar, Bangalore',       'Budget around 12L',             'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 155 DAY)),
(505, 'Eshan',    'Gupta',      'eshan.km@gmail.com',      '9880001005', '90 JP Nagar, Bangalore',        'Wants diesel variant',          'NEW',        505, 2, DATE_SUB(NOW(), INTERVAL 150 DAY)),
(506, 'Farida',   'Khan',       'farida.km@gmail.com',     '9880001006', '11 BTM Layout, Bangalore',      'Comparing with Nexon',          'LOST',       501, 2, DATE_SUB(NOW(), INTERVAL 145 DAY)),
(507, 'Ganesh',   'Iyer',       'ganesh.km@gmail.com',     '9880001007', '22 Banashankari, Bangalore',    'First time buyer',              'NEW',        502, 2, DATE_SUB(NOW(), INTERVAL 140 DAY)),
(508, 'Harini',   'Reddy',      'harini.km@gmail.com',     '9880001008', '33 Rajajinagar, Bangalore',     'Wants automatic transmission',  'INTERESTED', 503, 2, DATE_SUB(NOW(), INTERVAL 135 DAY)),
(509, 'Ishaan',   'Verma',      'ishaan.km@gmail.com',     '9880001009', '44 Malleshwaram, Bangalore',    'Corporate purchase',            'BOOKED',     504, 2, DATE_SUB(NOW(), INTERVAL 130 DAY)),
(510, 'Jaya',     'Krishnan',   'jaya.km@gmail.com',       '9880001010', '55 Sadashivanagar, Bangalore',  'Exchange offer discussed',      'INTERESTED', 505, 2, DATE_SUB(NOW(), INTERVAL 125 DAY)),
(511, 'Kiran',    'Bose',       'kiran.km@gmail.com',      '9880001011', '66 Hebbal, Bangalore',          'Wants i20 Asta',                'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(512, 'Lavanya',  'Pillai',     'lavanya.km@gmail.com',    '9880001012', '77 Yelahanka, Bangalore',       'Finance pre-approved',          'BOOKED',     502, 2, DATE_SUB(NOW(), INTERVAL 115 DAY)),
(513, 'Manoj',    'Singh',      'manoj.km@gmail.com',      '9880001013', '88 Devanahalli, Bangalore',     'Wants white color only',        'NEW',        503, 2, DATE_SUB(NOW(), INTERVAL 110 DAY)),
(514, 'Nandita',  'Rao',        'nandita.km@gmail.com',    '9880001014', '99 Whitefield, Bangalore',      'Interested in Tucson',          'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 105 DAY)),
(515, 'Om',       'Prakash',    'om.km@gmail.com',         '9880001015', '10 Marathahalli, Bangalore',    'Bought elsewhere',              'LOST',       505, 2, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(516, 'Pooja',    'Menon',      'pooja.km@gmail.com',      '9880001016', '21 Sarjapur, Bangalore',        'Wants Verna SX Turbo',          'BOOKED',     501, 2, DATE_SUB(NOW(), INTERVAL 95 DAY)),
(517, 'Qasim',    'Ali',        'qasim.km@gmail.com',      '9880001017', '32 Electronic City, Bangalore', 'Looking for EV options',        'NEW',        502, 2, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(518, 'Riya',     'Desai',      'riya.km@gmail.com',       '9880001018', '43 Bannerghatta, Bangalore',    'Wants Exter CNG',               'INTERESTED', 503, 2, DATE_SUB(NOW(), INTERVAL 85 DAY)),
(519, 'Sanjay',   'Kumar',      'sanjay.km@gmail.com',     '9880001019', '54 Kanakapura, Bangalore',      'Fleet purchase inquiry',        'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 80 DAY)),
(520, 'Tanvi',    'Shah',       'tanvi.km@gmail.com',      '9880001020', '65 Mysore Road, Bangalore',     'Wants sunroof variant',         'BOOKED',     505, 2, DATE_SUB(NOW(), INTERVAL 75 DAY)),
(521, 'Uday',     'Rajan',      'uday.km@gmail.com',       '9880001021', '76 Tumkur Road, Bangalore',     'Budget 8-10L',                  'NEW',        501, 2, DATE_SUB(NOW(), INTERVAL 70 DAY)),
(522, 'Vandana',  'Tiwari',     'vandana.km@gmail.com',    '9880001022', '87 Peenya, Bangalore',          'Interested in i20 Sportz',      'INTERESTED', 502, 2, DATE_SUB(NOW(), INTERVAL 65 DAY)),
(523, 'Wasim',    'Ansari',     'wasim.km@gmail.com',      '9880001023', '98 Yeshwanthpur, Bangalore',    'Needs 7-seater',                'LOST',       503, 2, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(524, 'Xena',     'Thomas',     'xena.km@gmail.com',       '9880001024', '19 Nagarbhavi, Bangalore',      'Wants Creta diesel',            'BOOKED',     504, 2, DATE_SUB(NOW(), INTERVAL 55 DAY)),
(525, 'Yogesh',   'Patil',      'yogesh.km@gmail.com',     '9880001025', '28 Vijayanagar, Bangalore',     'First car purchase',            'NEW',        505, 2, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(526, 'Zara',     'Hussain',    'zara.km@gmail.com',       '9880001026', '37 Basaveshwara Nagar, Blr',    'Wants Venue S',                 'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(527, 'Amit',     'Joshi',      'amit.km@gmail.com',       '9880001027', '46 Mathikere, Bangalore',       'Comparing Venue vs Exter',      'INTERESTED', 502, 2, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(528, 'Bindu',    'Nambiar',    'bindu.km@gmail.com',      '9880001028', '55 RT Nagar, Bangalore',        'Wants finance option',          'BOOKED',     503, 2, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(529, 'Chirag',   'Malhotra',   'chirag.km@gmail.com',     '9880001029', '64 Banaswadi, Bangalore',       'Interested in Creta E',         'NEW',        504, 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(530, 'Disha',    'Kapoor',     'disha.km@gmail.com',      '9880001030', '73 Kammanahalli, Bangalore',    'Wants Ioniq 5',                 'INTERESTED', 505, 2, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ==========================================================
-- SALES ORDERS (30 orders)
-- Uses actual columns: customer_id, vehicle_id, dealer_id,
-- price, discount, final_amount, status, created_at
-- lead_id, inventory_id, employee_id, total_amount = legacy NOT NULL cols
-- ==========================================================
INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES
-- INVOICED (12) - each inventory_id is unique (5001-5030)
(501, 1, 5001, 501, 503, 505, 2, 894000.00,  20000.00,  874000.00,  874000.00,  25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 160 DAY)),
(502, 1, 5002, 502, 509, 503, 2, 1499000.00, 30000.00, 1469000.00, 1469000.00,  50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 150 DAY)),
(503, 1, 5003, 503, 512, 511, 2,  699000.00, 10000.00,  689000.00,  689000.00,  20000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 140 DAY)),
(504, 1, 5004, 504, 516, 509, 2, 1299000.00, 25000.00, 1274000.00, 1274000.00,  40000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 130 DAY)),
(505, 1, 5005, 505, 520, 503, 2, 1499000.00, 35000.00, 1464000.00, 1464000.00,  50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 120 DAY)),
(506, 1, 5006, 501, 524, 504, 2, 1799000.00, 50000.00, 1749000.00, 1749000.00,  75000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 110 DAY)),
(507, 1, 5007, 502, 528, 508, 2, 1099000.00, 20000.00, 1079000.00, 1079000.00,  30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 100 DAY)),
(508, 1, 5008, 503, 501, 503, 2, 1499000.00, 30000.00, 1469000.00, 1469000.00,  50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 90 DAY)),
(509, 1, 5009, 504, 504, 506, 2,  894000.00, 15000.00,  879000.00,  879000.00,  25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 80 DAY)),
(510, 1, 5010, 505, 508, 513, 2,  949000.00, 20000.00,  929000.00,  929000.00,  25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 70 DAY)),
(511, 1, 5011, 501, 511, 512, 2,  799000.00, 10000.00,  789000.00,  789000.00,  20000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 60 DAY)),
(512, 1, 5012, 502, 514, 515, 2, 2799000.00, 75000.00, 2724000.00, 2724000.00, 100000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 50 DAY)),
-- CONFIRMED (9)
(513, 1, 5013, 503, 518, 519, 2,  799000.00, 10000.00,  789000.00,  789000.00,  20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 45 DAY)),
(514, 1, 5014, 504, 519, 505, 2,  794000.00,  5000.00,  789000.00,  789000.00,  20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 42 DAY)),
(515, 1, 5015, 505, 522, 513, 2,  949000.00, 15000.00,  934000.00,  934000.00,  25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 38 DAY)),
(516, 1, 5016, 501, 526, 506, 2,  894000.00, 10000.00,  884000.00,  884000.00,  25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 35 DAY)),
(517, 1, 5017, 502, 527, 517, 2,  599000.00,  0.00,     599000.00,  599000.00,  15000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(518, 1, 5018, 503, 529, 501, 2, 1099000.00, 15000.00, 1084000.00, 1084000.00,  30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(519, 1, 5019, 504, 530, 525, 2, 4499000.00,100000.00, 4399000.00, 4399000.00, 150000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(520, 1, 5020, 505, 502, 518, 2,  699000.00,  5000.00,  694000.00,  694000.00,  20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(521, 1, 5021, 501, 505, 504, 2, 1799000.00, 40000.00, 1759000.00, 1759000.00,  60000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 15 DAY)),
-- PENDING (6)
(522, 1, 5022, 502, 507, 511, 2,  699000.00,  0.00,     699000.00,  699000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 12 DAY)),
(523, 1, 5023, 503, 513, 501, 2, 1099000.00,  0.00,    1099000.00, 1099000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 10 DAY)),
(524, 1, 5024, 504, 517, 517, 2,  599000.00,  0.00,     599000.00,  599000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 8 DAY)),
(525, 1, 5025, 505, 521, 509, 2, 1299000.00,  0.00,    1299000.00, 1299000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 5 DAY)),
(526, 1, 5026, 501, 525, 505, 2,  794000.00,  0.00,     794000.00,  794000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 3 DAY)),
(527, 1, 5027, 502, 529, 503, 2, 1499000.00,  0.00,    1499000.00, 1499000.00,   0.00,    'PENDING',   DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- CANCELLED (3)
(528, 1, 5028, 503, 506, 507, 2, 1094000.00,  0.00,    1094000.00, 1094000.00,   0.00,    'CANCELLED', DATE_SUB(NOW(), INTERVAL 145 DAY)),
(529, 1, 5029, 504, 515, 516, 2, 2999000.00,  0.00,    2999000.00, 2999000.00,   0.00,    'CANCELLED', DATE_SUB(NOW(), INTERVAL 100 DAY)),
(530, 1, 5030, 505, 523, 510, 2, 1499000.00,  0.00,    1499000.00, 1499000.00,   0.00,    'CANCELLED', DATE_SUB(NOW(), INTERVAL 55 DAY));

-- ==========================================================
-- PAYMENTS (for INVOICED orders)
-- ==========================================================
INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES
(501,  874000.00, 'BANK_TRANSFER', 'TXN-KM-501', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 159 DAY)),
(502, 1469000.00, 'FINANCE',       'TXN-KM-502', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 149 DAY)),
(503,  689000.00, 'CASH',          'TXN-KM-503', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 139 DAY)),
(504, 1274000.00, 'BANK_TRANSFER', 'TXN-KM-504', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 129 DAY)),
(505, 1464000.00, 'FINANCE',       'TXN-KM-505', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 119 DAY)),
(506, 1749000.00, 'FINANCE',       'TXN-KM-506', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 109 DAY)),
(507, 1079000.00, 'BANK_TRANSFER', 'TXN-KM-507', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 99 DAY)),
(508, 1469000.00, 'FINANCE',       'TXN-KM-508', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 89 DAY)),
(509,  879000.00, 'CASH',          'TXN-KM-509', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 79 DAY)),
(510,  929000.00, 'CARD',          'TXN-KM-510', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 69 DAY)),
(511,  789000.00, 'BANK_TRANSFER', 'TXN-KM-511', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 59 DAY)),
(512, 2724000.00, 'FINANCE',       'TXN-KM-512', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 49 DAY));

-- ==========================================================
-- AUDIT LOGS
-- ==========================================================
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description, created_at) VALUES
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: kiran_motors',                DATE_SUB(NOW(), INTERVAL 170 DAY)),
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: kiran_motors',                DATE_SUB(NOW(), INTERVAL 90 DAY)),
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: kiran_motors',                DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1,    'LOGIN',         'SYSTEM',      1,    'User logged in: admin',                       DATE_SUB(NOW(), INTERVAL 1 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    501,  'Created customer: Aarav Mehta',               DATE_SUB(NOW(), INTERVAL 170 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    510,  'Created customer: Jaya Krishnan',             DATE_SUB(NOW(), INTERVAL 125 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    520,  'Created customer: Tanvi Shah',                DATE_SUB(NOW(), INTERVAL 75 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    530,  'Created customer: Disha Kapoor',              DATE_SUB(NOW(), INTERVAL 10 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 501,  'Order created - Venue E for Chetan Patel',    DATE_SUB(NOW(), INTERVAL 160 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 505,  'Order created - Creta SX for Tanvi Shah',     DATE_SUB(NOW(), INTERVAL 120 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 512,  'Order created - Tucson for Nandita Rao',      DATE_SUB(NOW(), INTERVAL 50 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 527,  'Order created - Creta SX for Chirag Malhotra',DATE_SUB(NOW(), INTERVAL 1 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 501,  'Order 501 status changed to INVOICED',        DATE_SUB(NOW(), INTERVAL 158 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 505,  'Order 505 status changed to INVOICED',        DATE_SUB(NOW(), INTERVAL 118 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 513,  'Order 513 status changed to CONFIRMED',       DATE_SUB(NOW(), INTERVAL 43 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 528,  'Order 528 status changed to CANCELLED',       DATE_SUB(NOW(), INTERVAL 143 DAY)),
(1,    'UPDATE',        'VEHICLE',     501,  'Stock updated for Creta E Petrol MT',          DATE_SUB(NOW(), INTERVAL 100 DAY)),
(1,    'UPDATE',        'VEHICLE',     515,  'Stock updated for Tucson Platinum',            DATE_SUB(NOW(), INTERVAL 50 DAY)),
(1,    'CREATE',        'USER',        501,  'Created EMPLOYEE: km_arjun',                  DATE_SUB(NOW(), INTERVAL 170 DAY)),
(1,    'CREATE',        'USER',        505,  'Created EMPLOYEE: km_karthik',                DATE_SUB(NOW(), INTERVAL 170 DAY));

-- ==========================================================
-- CLEANUP
-- ==========================================================
SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

SELECT 'Kiran Motors data loaded!' AS status,
  (SELECT COUNT(*) FROM employees WHERE dealer_id = 2) AS employees,
  (SELECT COUNT(*) FROM vehicles WHERE dealer_id = 2) AS vehicles,
  (SELECT COUNT(*) FROM customers WHERE dealer_id = 2) AS customers,
  (SELECT COUNT(*) FROM sales_orders WHERE dealer_id = 2) AS sales_orders;
