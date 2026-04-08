-- ==========================================================
-- Hyundai DMS - Realistic Sample Data
-- NEW DEALER: Rajesh Motors (dealer_id = 11)
-- Credentials: username=rajesh_motors  password=Rajesh@2024
-- ==========================================================

USE dms_db;
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- Clean up any partial data from previous runs
DELETE FROM audit_logs       WHERE entity_id >= 3001 OR (user_id IN (301,302,303,304,305,306,307,308,309,310,311,312));
DELETE FROM payments         WHERE sales_order_id BETWEEN 3001 AND 3060;
DELETE FROM sales_orders     WHERE id BETWEEN 3001 AND 3060;
DELETE FROM inventory        WHERE id BETWEEN 3001 AND 3025;
DELETE FROM vehicle_variants WHERE id BETWEEN 301 AND 310;
DELETE FROM vehicle_models   WHERE id BETWEEN 301 AND 306;
DELETE FROM customer_leads   WHERE id BETWEEN 3001 AND 3050;
DELETE FROM customers        WHERE id BETWEEN 3001 AND 3050;
DELETE FROM employees        WHERE id BETWEEN 301 AND 312;
DELETE FROM user_roles       WHERE user_id BETWEEN 301 AND 312;
DELETE FROM users            WHERE id BETWEEN 301 AND 312;
DELETE FROM departments      WHERE id BETWEEN 110 AND 114;
DELETE FROM branches         WHERE id BETWEEN 110 AND 111;

-- ==========================================================
-- STEP 1: BRANCHES & DEPARTMENTS (dealer_id = 11)
-- ==========================================================
INSERT INTO branches (id, dealer_id, name, location) VALUES
(110, 11, 'Rajesh Motors Main Showroom', 'MG Road, Bangalore'),
(111, 11, 'Rajesh Motors East Branch',   'Whitefield, Bangalore');

INSERT INTO departments (id, branch_id, name) VALUES
(110, 110, 'Sales'),
(111, 110, 'Service'),
(112, 110, 'Finance'),
(113, 111, 'Sales'),
(114, 111, 'Service');

-- ==========================================================
-- STEP 2: EMPLOYEE USERS (12 employees, dealer_id = 11)
-- All passwords: password123
-- ==========================================================
INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES
(301, 'rm_arjun',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'arjun@rajeshmotors.com',   'Arjun Sharma',   1, 11),
(302, 'rm_priya',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'priya@rajeshmotors.com',   'Priya Nair',     1, 11),
(303, 'rm_rahul',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'rahul@rajeshmotors.com',   'Rahul Verma',    1, 11),
(304, 'rm_sneha',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'sneha@rajeshmotors.com',   'Sneha Pillai',   1, 11),
(305, 'rm_karthik', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'karthik@rajeshmotors.com', 'Karthik Rajan',  1, 11),
(306, 'rm_divya',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'divya@rajeshmotors.com',   'Divya Menon',    1, 11),
(307, 'rm_vikram',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'vikram@rajeshmotors.com',  'Vikram Singh',   1, 11),
(308, 'rm_ananya',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ananya@rajeshmotors.com',  'Ananya Iyer',    1, 11),
(309, 'rm_suresh',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'suresh@rajeshmotors.com',  'Suresh Kumar',   1, 11),
(310, 'rm_meera',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'meera@rajeshmotors.com',   'Meera Krishnan', 1, 11),
(311, 'rm_aditya',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'aditya@rajeshmotors.com',  'Aditya Patel',   1, 11),
(312, 'rm_lakshmi', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'lakshmi@rajeshmotors.com', 'Lakshmi Devi',   1, 11);

INSERT INTO user_roles (user_id, role_id) VALUES
(301,3),(302,3),(303,3),(304,3),(305,3),(306,3),
(307,3),(308,3),(309,3),(310,3),(311,3),(312,3);

INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES
(301, 301, 110, 'EMP-11-301', 'Senior Sales Executive', '2022-03-15', 11),
(302, 302, 110, 'EMP-11-302', 'Sales Executive',        '2022-07-01', 11),
(303, 303, 110, 'EMP-11-303', 'Sales Executive',        '2023-01-10', 11),
(304, 304, 110, 'EMP-11-304', 'Sales Consultant',       '2023-04-20', 11),
(305, 305, 113, 'EMP-11-305', 'Senior Sales Executive', '2021-11-05', 11),
(306, 306, 111, 'EMP-11-306', 'Service Advisor',        '2022-09-12', 11),
(307, 307, 111, 'EMP-11-307', 'Service Technician',     '2023-02-28', 11),
(308, 308, 113, 'EMP-11-308', 'Sales Consultant',       '2023-06-15', 11),
(309, 309, 112, 'EMP-11-309', 'Finance Manager',        '2021-08-01', 11),
(310, 310, 110, 'EMP-11-310', 'Sales Executive',        '2024-01-08', 11),
(311, 311, 113, 'EMP-11-311', 'Sales Executive',        '2024-02-14', 11),
(312, 312, 112, 'EMP-11-312', 'Finance Executive',      '2023-10-01', 11);

-- ==========================================================
-- STEP 3: CUSTOMERS (50 customers, dealer_id = 11)
-- ==========================================================
INSERT INTO customers (id, first_name, last_name, email, phone, address, dealer_id, created_at) VALUES
(3001, 'Aarav',    'Mehta',       'aarav.mehta11@gmail.com',     '9900110001', '12 Indiranagar, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 165 DAY)),
(3002, 'Bhavna',   'Sharma',      'bhavna.sharma11@gmail.com',   '9900110002', '34 Koramangala, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 160 DAY)),
(3003, 'Chetan',   'Patel',       'chetan.patel11@gmail.com',    '9900110003', '56 HSR Layout, Bangalore',       11, DATE_SUB(NOW(), INTERVAL 155 DAY)),
(3004, 'Deepika',  'Nair',        'deepika.nair11@gmail.com',    '9900110004', '78 Jayanagar, Bangalore',        11, DATE_SUB(NOW(), INTERVAL 150 DAY)),
(3005, 'Eshan',    'Gupta',       'eshan.gupta11@gmail.com',     '9900110005', '90 JP Nagar, Bangalore',         11, DATE_SUB(NOW(), INTERVAL 148 DAY)),
(3006, 'Farida',   'Khan',        'farida.khan11@gmail.com',     '9900110006', '11 BTM Layout, Bangalore',       11, DATE_SUB(NOW(), INTERVAL 145 DAY)),
(3007, 'Ganesh',   'Iyer',        'ganesh.iyer11@gmail.com',     '9900110007', '22 Banashankari, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 142 DAY)),
(3008, 'Harini',   'Reddy',       'harini.reddy11@gmail.com',    '9900110008', '33 Rajajinagar, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 140 DAY)),
(3009, 'Ishaan',   'Verma',       'ishaan.verma11@gmail.com',    '9900110009', '44 Malleshwaram, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 138 DAY)),
(3010, 'Jaya',     'Krishnan',    'jaya.krishnan11@gmail.com',   '9900110010', '55 Sadashivanagar, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 135 DAY)),
(3011, 'Kiran',    'Bose',        'kiran.bose11@gmail.com',      '9900110011', '66 Hebbal, Bangalore',           11, DATE_SUB(NOW(), INTERVAL 132 DAY)),
(3012, 'Lavanya',  'Pillai',      'lavanya.pillai11@gmail.com',  '9900110012', '77 Yelahanka, Bangalore',        11, DATE_SUB(NOW(), INTERVAL 130 DAY)),
(3013, 'Manoj',    'Singh',       'manoj.singh11@gmail.com',     '9900110013', '88 Devanahalli, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 128 DAY)),
(3014, 'Nandita',  'Rao',         'nandita.rao11@gmail.com',     '9900110014', '99 Whitefield, Bangalore',       11, DATE_SUB(NOW(), INTERVAL 125 DAY)),
(3015, 'Om',       'Prakash',     'om.prakash11@gmail.com',      '9900110015', '10 Marathahalli, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 122 DAY)),
(3016, 'Pooja',    'Menon',       'pooja.menon11@gmail.com',     '9900110016', '21 Sarjapur, Bangalore',         11, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3017, 'Qasim',    'Ali',         'qasim.ali11@gmail.com',       '9900110017', '32 Electronic City, Bangalore',  11, DATE_SUB(NOW(), INTERVAL 118 DAY)),
(3018, 'Riya',     'Desai',       'riya.desai11@gmail.com',      '9900110018', '43 Bannerghatta, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 115 DAY)),
(3019, 'Sanjay',   'Kumar',       'sanjay.kumar11@gmail.com',    '9900110019', '54 Kanakapura, Bangalore',       11, DATE_SUB(NOW(), INTERVAL 112 DAY)),
(3020, 'Tanvi',    'Shah',        'tanvi.shah11@gmail.com',      '9900110020', '65 Mysore Road, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 110 DAY)),
(3021, 'Uday',     'Rajan',       'uday.rajan11@gmail.com',      '9900110021', '76 Tumkur Road, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 108 DAY)),
(3022, 'Vandana',  'Tiwari',      'vandana.tiwari11@gmail.com',  '9900110022', '87 Peenya, Bangalore',           11, DATE_SUB(NOW(), INTERVAL 105 DAY)),
(3023, 'Wasim',    'Ansari',      'wasim.ansari11@gmail.com',    '9900110023', '98 Yeshwanthpur, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 102 DAY)),
(3024, 'Xena',     'Thomas',      'xena.thomas11@gmail.com',     '9900110024', '19 Nagarbhavi, Bangalore',       11, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3025, 'Yogesh',   'Patil',       'yogesh.patil11@gmail.com',    '9900110025', '28 Vijayanagar, Bangalore',      11, DATE_SUB(NOW(), INTERVAL 98 DAY)),
(3026, 'Zara',     'Hussain',     'zara.hussain11@gmail.com',    '9900110026', '37 Basaveshwara Nagar, Blr',     11, DATE_SUB(NOW(), INTERVAL 95 DAY)),
(3027, 'Amit',     'Joshi',       'amit.joshi11@gmail.com',      '9900110027', '46 Mathikere, Bangalore',        11, DATE_SUB(NOW(), INTERVAL 92 DAY)),
(3028, 'Bindu',    'Nambiar',     'bindu.nambiar11@gmail.com',   '9900110028', '55 RT Nagar, Bangalore',         11, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3029, 'Chirag',   'Malhotra',    'chirag.malhotra11@gmail.com', '9900110029', '64 Banaswadi, Bangalore',        11, DATE_SUB(NOW(), INTERVAL 88 DAY)),
(3030, 'Disha',    'Kapoor',      'disha.kapoor11@gmail.com',    '9900110030', '73 Kammanahalli, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 85 DAY)),
(3031, 'Ekta',     'Srivastava',  'ekta.sriv11@gmail.com',       '9900110031', '82 CV Raman Nagar, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 82 DAY)),
(3032, 'Farhan',   'Sheikh',      'farhan.sheikh11@gmail.com',   '9900110032', '91 Domlur, Bangalore',           11, DATE_SUB(NOW(), INTERVAL 80 DAY)),
(3033, 'Geeta',    'Mishra',      'geeta.mishra11@gmail.com',    '9900110033', '100 Ejipura, Bangalore',         11, DATE_SUB(NOW(), INTERVAL 78 DAY)),
(3034, 'Hemant',   'Dubey',       'hemant.dubey11@gmail.com',    '9900110034', '109 Vivek Nagar, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 75 DAY)),
(3035, 'Indira',   'Nair',        'indira.nair11@gmail.com',     '9900110035', '118 Shivajinagar, Bangalore',    11, DATE_SUB(NOW(), INTERVAL 72 DAY)),
(3036, 'Jai',      'Agarwal',     'jai.agarwal11@gmail.com',     '9900110036', '127 Frazer Town, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 70 DAY)),
(3037, 'Kavya',    'Subramaniam', 'kavya.sub11@gmail.com',       '9900110037', '136 Richmond Town, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 68 DAY)),
(3038, 'Lokesh',   'Pandey',      'lokesh.pandey11@gmail.com',   '9900110038', '145 Langford Town, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 65 DAY)),
(3039, 'Mala',     'Venkat',      'mala.venkat11@gmail.com',     '9900110039', '154 Vasanth Nagar, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 62 DAY)),
(3040, 'Nikhil',   'Chandra',     'nikhil.chandra11@gmail.com',  '9900110040', '163 Cunningham Road, Bangalore', 11, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3041, 'Ojasvi',   'Rawat',       'ojasvi.rawat11@gmail.com',    '9900110041', '172 MG Road, Bangalore',         11, DATE_SUB(NOW(), INTERVAL 55 DAY)),
(3042, 'Pallavi',  'Jain',        'pallavi.jain11@gmail.com',    '9900110042', '181 Brigade Road, Bangalore',    11, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(3043, 'Qadir',    'Mirza',       'qadir.mirza11@gmail.com',     '9900110043', '190 Commercial Street, Blr',     11, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(3044, 'Rekha',    'Pillai',      'rekha.pillai11@gmail.com',    '9900110044', '199 Residency Road, Bangalore',  11, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3045, 'Sachin',   'Tendulkar',   'sachin.t11@gmail.com',        '9900110045', '208 Lavelle Road, Bangalore',    11, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(3046, 'Tara',     'Bhatia',      'tara.bhatia11@gmail.com',     '9900110046', '217 St Marks Road, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3047, 'Umesh',    'Yadav',       'umesh.yadav11@gmail.com',     '9900110047', '226 Kasturba Road, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3048, 'Vidya',    'Balan',       'vidya.b11@gmail.com',         '9900110048', '235 Cubbon Park Road, Bangalore',11, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3049, 'Waqar',    'Younis',      'waqar.y11@gmail.com',         '9900110049', '244 Seshadri Road, Bangalore',   11, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3050, 'Yuvraj',   'Singh',       'yuvraj.s11@gmail.com',        '9900110050', '253 Gandhinagar, Bangalore',     11, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ==========================================================
-- STEP 4: CUSTOMER LEADS (one per customer)
-- ==========================================================
INSERT INTO customer_leads (id, customer_id, source, status, assigned_employee_id, created_at) VALUES
(3001, 3001, 'Walk-in',  'QUALIFIED',  301, DATE_SUB(NOW(), INTERVAL 165 DAY)),
(3002, 3002, 'Website',  'NEW',        302, DATE_SUB(NOW(), INTERVAL 160 DAY)),
(3003, 3003, 'Referral', 'CONVERTED',  303, DATE_SUB(NOW(), INTERVAL 155 DAY)),
(3004, 3004, 'Walk-in',  'QUALIFIED',  304, DATE_SUB(NOW(), INTERVAL 150 DAY)),
(3005, 3005, 'Website',  'NEW',        305, DATE_SUB(NOW(), INTERVAL 148 DAY)),
(3006, 3006, 'Walk-in',  'LOST',       301, DATE_SUB(NOW(), INTERVAL 145 DAY)),
(3007, 3007, 'Referral', 'NEW',        302, DATE_SUB(NOW(), INTERVAL 142 DAY)),
(3008, 3008, 'Website',  'QUALIFIED',  303, DATE_SUB(NOW(), INTERVAL 140 DAY)),
(3009, 3009, 'Walk-in',  'CONVERTED',  304, DATE_SUB(NOW(), INTERVAL 138 DAY)),
(3010, 3010, 'Referral', 'QUALIFIED',  305, DATE_SUB(NOW(), INTERVAL 135 DAY)),
(3011, 3011, 'Walk-in',  'QUALIFIED',  301, DATE_SUB(NOW(), INTERVAL 132 DAY)),
(3012, 3012, 'Website',  'CONVERTED',  302, DATE_SUB(NOW(), INTERVAL 130 DAY)),
(3013, 3013, 'Walk-in',  'NEW',        303, DATE_SUB(NOW(), INTERVAL 128 DAY)),
(3014, 3014, 'Referral', 'QUALIFIED',  304, DATE_SUB(NOW(), INTERVAL 125 DAY)),
(3015, 3015, 'Website',  'LOST',       305, DATE_SUB(NOW(), INTERVAL 122 DAY)),
(3016, 3016, 'Walk-in',  'CONVERTED',  301, DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3017, 3017, 'Website',  'NEW',        302, DATE_SUB(NOW(), INTERVAL 118 DAY)),
(3018, 3018, 'Referral', 'QUALIFIED',  303, DATE_SUB(NOW(), INTERVAL 115 DAY)),
(3019, 3019, 'Walk-in',  'QUALIFIED',  304, DATE_SUB(NOW(), INTERVAL 112 DAY)),
(3020, 3020, 'Website',  'CONVERTED',  305, DATE_SUB(NOW(), INTERVAL 110 DAY)),
(3021, 3021, 'Walk-in',  'NEW',        301, DATE_SUB(NOW(), INTERVAL 108 DAY)),
(3022, 3022, 'Referral', 'QUALIFIED',  302, DATE_SUB(NOW(), INTERVAL 105 DAY)),
(3023, 3023, 'Website',  'LOST',       303, DATE_SUB(NOW(), INTERVAL 102 DAY)),
(3024, 3024, 'Walk-in',  'CONVERTED',  304, DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3025, 3025, 'Referral', 'NEW',        305, DATE_SUB(NOW(), INTERVAL 98 DAY)),
(3026, 3026, 'Website',  'QUALIFIED',  301, DATE_SUB(NOW(), INTERVAL 95 DAY)),
(3027, 3027, 'Walk-in',  'QUALIFIED',  302, DATE_SUB(NOW(), INTERVAL 92 DAY)),
(3028, 3028, 'Referral', 'CONVERTED',  303, DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3029, 3029, 'Website',  'NEW',        304, DATE_SUB(NOW(), INTERVAL 88 DAY)),
(3030, 3030, 'Walk-in',  'QUALIFIED',  305, DATE_SUB(NOW(), INTERVAL 85 DAY)),
(3031, 3031, 'Referral', 'CONVERTED',  301, DATE_SUB(NOW(), INTERVAL 82 DAY)),
(3032, 3032, 'Website',  'QUALIFIED',  302, DATE_SUB(NOW(), INTERVAL 80 DAY)),
(3033, 3033, 'Walk-in',  'CONVERTED',  303, DATE_SUB(NOW(), INTERVAL 78 DAY)),
(3034, 3034, 'Referral', 'QUALIFIED',  304, DATE_SUB(NOW(), INTERVAL 75 DAY)),
(3035, 3035, 'Website',  'LOST',       305, DATE_SUB(NOW(), INTERVAL 72 DAY)),
(3036, 3036, 'Walk-in',  'CONVERTED',  301, DATE_SUB(NOW(), INTERVAL 70 DAY)),
(3037, 3037, 'Referral', 'NEW',        302, DATE_SUB(NOW(), INTERVAL 68 DAY)),
(3038, 3038, 'Website',  'QUALIFIED',  303, DATE_SUB(NOW(), INTERVAL 65 DAY)),
(3039, 3039, 'Walk-in',  'CONVERTED',  304, DATE_SUB(NOW(), INTERVAL 62 DAY)),
(3040, 3040, 'Referral', 'QUALIFIED',  305, DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3041, 3041, 'Website',  'NEW',        301, DATE_SUB(NOW(), INTERVAL 55 DAY)),
(3042, 3042, 'Walk-in',  'CONVERTED',  302, DATE_SUB(NOW(), INTERVAL 50 DAY)),
(3043, 3043, 'Referral', 'QUALIFIED',  303, DATE_SUB(NOW(), INTERVAL 45 DAY)),
(3044, 3044, 'Website',  'CONVERTED',  304, DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3045, 3045, 'Walk-in',  'QUALIFIED',  305, DATE_SUB(NOW(), INTERVAL 35 DAY)),
(3046, 3046, 'Referral', 'CONVERTED',  301, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3047, 3047, 'Website',  'LOST',       302, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3048, 3048, 'Walk-in',  'QUALIFIED',  303, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3049, 3049, 'Referral', 'NEW',        304, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3050, 3050, 'Website',  'CONVERTED',  305, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- ==========================================================
-- STEP 5: VEHICLE MODELS + VARIANTS
-- vehicle_variants needs BOTH old cols (variant_name, vehicle_id)
-- AND new Hibernate cols (name, model_id)
-- ==========================================================
INSERT INTO vehicle_models (id, name) VALUES
(301, 'Creta'),
(302, 'Venue'),
(303, 'Verna'),
(304, 'i20'),
(305, 'Tucson'),
(306, 'Exter');

-- variant_name = old schema col, name = Hibernate col, vehicle_id = old FK, model_id = Hibernate FK
INSERT INTO vehicle_variants (id, vehicle_id, variant_name, fuel_type, transmission, additional_cost, name, model_id) VALUES
(301, 1, 'E Petrol MT',        'Petrol', 'MT',  0.00,      'E Petrol MT',        301),
(302, 1, 'S Petrol MT',        'Petrol', 'MT',  100000.00, 'S Petrol MT',        301),
(303, 1, 'SX Petrol AT',       'Petrol', 'AT',  400000.00, 'SX Petrol AT',       301),
(304, 1, 'SX(O) Diesel AT',    'Diesel', 'AT',  700000.00, 'SX(O) Diesel AT',    301),
(305, 2, 'E Petrol MT',        'Petrol', 'MT',  0.00,      'E Petrol MT',        302),
(306, 2, 'S Petrol MT',        'Petrol', 'MT',  100000.00, 'S Petrol MT',        302),
(307, 2, 'SX Turbo DCT',       'Petrol', 'DCT', 300000.00, 'SX Turbo DCT',       302),
(308, 3, 'EX Petrol MT',       'Petrol', 'MT',  0.00,      'EX Petrol MT',       303),
(309, 3, 'S Petrol IVT',       'Petrol', 'IVT', 200000.00, 'S Petrol IVT',       303),
(310, 3, 'SX Turbo DCT',       'Petrol', 'DCT', 400000.00, 'SX Turbo DCT',       303);

-- ==========================================================
-- STEP 6: INVENTORY (25 units for dealer_id = 11)
-- ==========================================================
INSERT INTO inventory (id, variant_id, dealer_id, vin, engine_number, color, status, arrival_date) VALUES
(3001, 301, 11, 'RM11CRET001E001', 'ENG-RM-C001', 'Phantom Black',  'AVAILABLE', DATE_SUB(NOW(), INTERVAL 180 DAY)),
(3002, 301, 11, 'RM11CRET001E002', 'ENG-RM-C002', 'Polar White',    'AVAILABLE', DATE_SUB(NOW(), INTERVAL 175 DAY)),
(3003, 302, 11, 'RM11CRET002S001', 'ENG-RM-C003', 'Typhoon Silver', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 170 DAY)),
(3004, 302, 11, 'RM11CRET002S002', 'ENG-RM-C004', 'Fiery Red',      'AVAILABLE', DATE_SUB(NOW(), INTERVAL 168 DAY)),
(3005, 303, 11, 'RM11CRET003X001', 'ENG-RM-C005', 'Starry Night',   'AVAILABLE', DATE_SUB(NOW(), INTERVAL 165 DAY)),
(3006, 303, 11, 'RM11CRET003X002', 'ENG-RM-C006', 'Atlas White',    'SOLD',      DATE_SUB(NOW(), INTERVAL 160 DAY)),
(3007, 304, 11, 'RM11CRET004D001', 'ENG-RM-C007', 'Phantom Black',  'SOLD',      DATE_SUB(NOW(), INTERVAL 158 DAY)),
(3008, 305, 11, 'RM11VENU005E001', 'ENG-RM-V001', 'Polar White',    'AVAILABLE', DATE_SUB(NOW(), INTERVAL 155 DAY)),
(3009, 305, 11, 'RM11VENU005E002', 'ENG-RM-V002', 'Typhoon Silver', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 152 DAY)),
(3010, 306, 11, 'RM11VENU006S001', 'ENG-RM-V003', 'Fiery Red',      'SOLD',      DATE_SUB(NOW(), INTERVAL 150 DAY)),
(3011, 306, 11, 'RM11VENU006S002', 'ENG-RM-V004', 'Denim Blue',     'AVAILABLE', DATE_SUB(NOW(), INTERVAL 148 DAY)),
(3012, 307, 11, 'RM11VENU007T001', 'ENG-RM-V005', 'Phantom Black',  'SOLD',      DATE_SUB(NOW(), INTERVAL 145 DAY)),
(3013, 308, 11, 'RM11VERN008E001', 'ENG-RM-VR01', 'Polar White',    'AVAILABLE', DATE_SUB(NOW(), INTERVAL 142 DAY)),
(3014, 308, 11, 'RM11VERN008E002', 'ENG-RM-VR02', 'Starry Night',   'SOLD',      DATE_SUB(NOW(), INTERVAL 140 DAY)),
(3015, 309, 11, 'RM11VERN009S001', 'ENG-RM-VR03', 'Typhoon Silver', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 138 DAY)),
(3016, 309, 11, 'RM11VERN009S002', 'ENG-RM-VR04', 'Fiery Red',      'SOLD',      DATE_SUB(NOW(), INTERVAL 135 DAY)),
(3017, 310, 11, 'RM11VERN010X001', 'ENG-RM-VR05', 'Atlas White',    'AVAILABLE', DATE_SUB(NOW(), INTERVAL 132 DAY)),
(3018, 310, 11, 'RM11VERN010X002', 'ENG-RM-VR06', 'Phantom Black',  'SOLD',      DATE_SUB(NOW(), INTERVAL 130 DAY)),
(3019, 301, 11, 'RM11CRET001E003', 'ENG-RM-C008', 'Denim Blue',     'AVAILABLE', DATE_SUB(NOW(), INTERVAL 128 DAY)),
(3020, 302, 11, 'RM11CRET002S003', 'ENG-RM-C009', 'Polar White',    'SOLD',      DATE_SUB(NOW(), INTERVAL 125 DAY)),
(3021, 303, 11, 'RM11CRET003X003', 'ENG-RM-C010', 'Fiery Red',      'AVAILABLE', DATE_SUB(NOW(), INTERVAL 122 DAY)),
(3022, 305, 11, 'RM11VENU005E003', 'ENG-RM-V006', 'Atlas White',    'SOLD',      DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3023, 306, 11, 'RM11VENU006S003', 'ENG-RM-V007', 'Typhoon Silver', 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 118 DAY)),
(3024, 308, 11, 'RM11VERN008E003', 'ENG-RM-VR07', 'Polar White',    'SOLD',      DATE_SUB(NOW(), INTERVAL 115 DAY)),
(3025, 309, 11, 'RM11VERN009S003', 'ENG-RM-VR08', 'Phantom Black',  'AVAILABLE', DATE_SUB(NOW(), INTERVAL 112 DAY));

-- ==========================================================
-- STEP 7: SALES ORDERS (60 orders - all required columns)
-- lead_id, inventory_id, employee_id = old NOT NULL cols
-- customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount = Hibernate cols
-- ==========================================================
INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES
-- INVOICED (24)
(3001, 3003, 3006, 303, 3003, 1, 11, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 155 DAY)),
(3002, 3009, 3007, 304, 3009, 1, 11, 1799000.00, 50000.00, 1749000.00, 1749000.00, 75000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 138 DAY)),
(3003, 3012, 3010, 302, 3012, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 130 DAY)),
(3004, 3016, 3014, 301, 3016, 3, 11, 1099000.00, 20000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3005, 3020, 3012, 305, 3020, 2, 11, 1094000.00, 15000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 110 DAY)),
(3006, 3024, 3016, 304, 3024, 3, 11, 1299000.00, 25000.00, 1274000.00, 1274000.00, 40000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3007, 3028, 3018, 303, 3028, 3, 11, 1499000.00, 35000.00, 1464000.00, 1464000.00, 50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3008, 3031, 3020, 301, 3031, 1, 11, 1199000.00, 20000.00, 1179000.00, 1179000.00, 35000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 82 DAY)),
(3009, 3033, 3022, 303, 3033, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 78 DAY)),
(3010, 3036, 3024, 301, 3036, 3, 11, 1099000.00, 15000.00, 1084000.00, 1084000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 70 DAY)),
(3011, 3039, 3006, 304, 3039, 1, 11, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 62 DAY)),
(3012, 3042, 3007, 302, 3042, 1, 11, 1799000.00, 45000.00, 1754000.00, 1754000.00, 60000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 50 DAY)),
(3013, 3044, 3010, 304, 3044, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 40 DAY)),
(3014, 3046, 3012, 301, 3046, 2, 11, 1094000.00, 15000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3015, 3050, 3014, 305, 3050, 3, 11, 1099000.00, 20000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3016, 3001, 3016, 301, 3001, 3, 11, 1299000.00, 25000.00, 1274000.00, 1274000.00, 40000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 158 DAY)),
(3017, 3004, 3018, 304, 3004, 3, 11, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 148 DAY)),
(3018, 3008, 3020, 303, 3008, 1, 11, 1199000.00, 20000.00, 1179000.00, 1179000.00, 35000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 140 DAY)),
(3019, 3010, 3022, 305, 3010, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 133 DAY)),
(3020, 3011, 3024, 301, 3011, 3, 11, 1099000.00, 15000.00, 1084000.00, 1084000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 128 DAY)),
(3021, 3014, 3006, 304, 3014, 1, 11, 1499000.00, 35000.00, 1464000.00, 1464000.00, 50000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 122 DAY)),
(3022, 3018, 3007, 303, 3018, 1, 11, 1799000.00, 50000.00, 1749000.00, 1749000.00, 75000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 113 DAY)),
(3023, 3019, 3010, 302, 3019, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 108 DAY)),
(3024, 3022, 3012, 301, 3022, 2, 11, 1094000.00, 15000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED',  DATE_SUB(NOW(), INTERVAL 103 DAY)),
-- CONFIRMED (18)
(3025, 3026, 3001, 301, 3026, 1, 11, 1099000.00, 10000.00, 1089000.00, 1089000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 93 DAY)),
(3026, 3027, 3002, 302, 3027, 1, 11, 1099000.00,  5000.00, 1094000.00, 1094000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3027, 3029, 3003, 304, 3029, 1, 11, 1199000.00, 15000.00, 1184000.00, 1184000.00, 35000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 86 DAY)),
(3028, 3030, 3004, 305, 3030, 1, 11, 1199000.00, 10000.00, 1189000.00, 1189000.00, 35000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 83 DAY)),
(3029, 3032, 3005, 302, 3032, 1, 11, 1499000.00, 20000.00, 1479000.00, 1479000.00, 50000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 78 DAY)),
(3030, 3034, 3008, 304, 3034, 2, 11,  794000.00, 10000.00,  784000.00,  784000.00, 20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 73 DAY)),
(3031, 3037, 3009, 302, 3037, 2, 11,  794000.00,  5000.00,  789000.00,  789000.00, 20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 66 DAY)),
(3032, 3038, 3011, 303, 3038, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 63 DAY)),
(3033, 3040, 3013, 305, 3040, 3, 11, 1099000.00, 15000.00, 1084000.00, 1084000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 58 DAY)),
(3034, 3041, 3015, 301, 3041, 3, 11, 1299000.00,  0.00,    1299000.00, 1299000.00, 40000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 53 DAY)),
(3035, 3043, 3017, 303, 3043, 3, 11, 1499000.00, 25000.00, 1474000.00, 1474000.00, 50000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 43 DAY)),
(3036, 3045, 3019, 305, 3045, 1, 11, 1099000.00, 20000.00, 1079000.00, 1079000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 33 DAY)),
(3037, 3048, 3021, 303, 3048, 1, 11, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3038, 3049, 3023, 304, 3049, 2, 11,  894000.00, 10000.00,  884000.00,  884000.00, 25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(3039, 3002, 3025, 302, 3002, 3, 11, 1299000.00,  5000.00, 1294000.00, 1294000.00, 40000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 158 DAY)),
(3040, 3005, 3001, 305, 3005, 1, 11, 1099000.00, 10000.00, 1089000.00, 1089000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 146 DAY)),
(3041, 3007, 3002, 301, 3007, 1, 11, 1099000.00, 10000.00, 1089000.00, 1089000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 140 DAY)),
(3042, 3013, 3003, 303, 3013, 1, 11, 1199000.00, 15000.00, 1184000.00, 1184000.00, 35000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 126 DAY)),
-- PENDING (12)
(3043, 3017, 3004, 302, 3017, 1, 11, 1199000.00, 0.00, 1199000.00, 1199000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 116 DAY)),
(3044, 3021, 3005, 301, 3021, 1, 11, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 106 DAY)),
(3045, 3025, 3008, 305, 3025, 2, 11,  794000.00, 0.00,  794000.00,  794000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 96 DAY)),
(3046, 3006, 3009, 301, 3006, 2, 11,  794000.00, 0.00,  794000.00,  794000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 143 DAY)),
(3047, 3015, 3011, 304, 3015, 2, 11,  894000.00, 0.00,  894000.00,  894000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3048, 3023, 3013, 303, 3023, 3, 11, 1099000.00, 0.00, 1099000.00, 1099000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3049, 3035, 3015, 305, 3035, 3, 11, 1299000.00, 0.00, 1299000.00, 1299000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 70 DAY)),
(3050, 3047, 3017, 302, 3047, 3, 11, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 23 DAY)),
(3051, 3006, 3019, 301, 3006, 1, 11, 1099000.00, 0.00, 1099000.00, 1099000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3052, 3015, 3021, 304, 3015, 1, 11, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3053, 3023, 3023, 303, 3023, 2, 11,  894000.00, 0.00,  894000.00,  894000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3054, 3035, 3025, 305, 3035, 3, 11, 1299000.00, 0.00, 1299000.00, 1299000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY)),
-- CANCELLED (6)
(3055, 3006, 3001, 301, 3006, 1, 11, 1099000.00, 0.00, 1099000.00, 1099000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 143 DAY)),
(3056, 3015, 3002, 304, 3015, 1, 11, 1099000.00, 0.00, 1099000.00, 1099000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3057, 3023, 3003, 303, 3023, 1, 11, 1199000.00, 0.00, 1199000.00, 1199000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 100 DAY)),
(3058, 3035, 3004, 305, 3035, 1, 11, 1199000.00, 0.00, 1199000.00, 1199000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 70 DAY)),
(3059, 3047, 3005, 302, 3047, 1, 11, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 23 DAY)),
(3060, 3006, 3008, 301, 3006, 2, 11,  794000.00, 0.00,  794000.00,  794000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ==========================================================
-- STEP 8: PAYMENTS (INVOICED orders only)
-- ==========================================================
INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES
(3001, 1469000.00, 'FINANCE',       'TXN-RM-3001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 154 DAY)),
(3002, 1749000.00, 'FINANCE',       'TXN-RM-3002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 137 DAY)),
(3003,  884000.00, 'CASH',          'TXN-RM-3003', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 129 DAY)),
(3004, 1079000.00, 'BANK_TRANSFER', 'TXN-RM-3004', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 119 DAY)),
(3005, 1079000.00, 'FINANCE',       'TXN-RM-3005', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 109 DAY)),
(3006, 1274000.00, 'BANK_TRANSFER', 'TXN-RM-3006', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 99 DAY)),
(3007, 1464000.00, 'FINANCE',       'TXN-RM-3007', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 89 DAY)),
(3008, 1179000.00, 'CASH',          'TXN-RM-3008', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 81 DAY)),
(3009,  884000.00, 'CARD',          'TXN-RM-3009', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 77 DAY)),
(3010, 1084000.00, 'BANK_TRANSFER', 'TXN-RM-3010', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 69 DAY)),
(3011, 1469000.00, 'FINANCE',       'TXN-RM-3011', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 61 DAY)),
(3012, 1754000.00, 'FINANCE',       'TXN-RM-3012', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 49 DAY)),
(3013,  884000.00, 'CASH',          'TXN-RM-3013', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 39 DAY)),
(3014, 1079000.00, 'BANK_TRANSFER', 'TXN-RM-3014', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 29 DAY)),
(3015, 1079000.00, 'FINANCE',       'TXN-RM-3015', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3016, 1274000.00, 'FINANCE',       'TXN-RM-3016', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 157 DAY)),
(3017, 1469000.00, 'BANK_TRANSFER', 'TXN-RM-3017', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 147 DAY)),
(3018, 1179000.00, 'CASH',          'TXN-RM-3018', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 139 DAY)),
(3019,  884000.00, 'FINANCE',       'TXN-RM-3019', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 132 DAY)),
(3020, 1084000.00, 'CARD',          'TXN-RM-3020', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 127 DAY)),
(3021, 1464000.00, 'FINANCE',       'TXN-RM-3021', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 121 DAY)),
(3022, 1749000.00, 'FINANCE',       'TXN-RM-3022', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 112 DAY)),
(3023,  884000.00, 'CASH',          'TXN-RM-3023', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 107 DAY)),
(3024, 1079000.00, 'BANK_TRANSFER', 'TXN-RM-3024', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 102 DAY));

-- ==========================================================
-- STEP 9: AUDIT LOGS
-- ==========================================================
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description, created_at) VALUES
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: rajesh_motors',                  DATE_SUB(NOW(), INTERVAL 165 DAY)),
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: rajesh_motors',                  DATE_SUB(NOW(), INTERVAL 90 DAY)),
(NULL, 'LOGIN',         'SYSTEM',      NULL, 'User logged in: rajesh_motors',                  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1,    'LOGIN',         'SYSTEM',      1,    'User logged in: admin',                          DATE_SUB(NOW(), INTERVAL 1 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    3001, 'Created customer: Aarav Mehta',                  DATE_SUB(NOW(), INTERVAL 165 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    3020, 'Created customer: Tanvi Shah',                   DATE_SUB(NOW(), INTERVAL 110 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    3040, 'Created customer: Nikhil Chandra',               DATE_SUB(NOW(), INTERVAL 60 DAY)),
(NULL, 'CREATE',        'CUSTOMER',    3050, 'Created customer: Yuvraj Singh',                 DATE_SUB(NOW(), INTERVAL 10 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 3001, 'Order created - Creta SX for Chetan Patel',      DATE_SUB(NOW(), INTERVAL 155 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 3005, 'Order created - Venue SX for Tanvi Shah',        DATE_SUB(NOW(), INTERVAL 110 DAY)),
(NULL, 'CREATE',        'SALES_ORDER', 3015, 'Order created - Verna S for Yuvraj Singh',       DATE_SUB(NOW(), INTERVAL 10 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 3001, 'Order 3001 status changed to INVOICED',          DATE_SUB(NOW(), INTERVAL 153 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 3025, 'Order 3025 status changed to CONFIRMED',         DATE_SUB(NOW(), INTERVAL 91 DAY)),
(NULL, 'UPDATE_STATUS', 'SALES_ORDER', 3055, 'Order 3055 status changed to CANCELLED',         DATE_SUB(NOW(), INTERVAL 141 DAY)),
(1,    'CREATE',        'USER',        301,  'Created EMPLOYEE: rm_arjun',                     DATE_SUB(NOW(), INTERVAL 165 DAY)),
(1,    'CREATE',        'USER',        305,  'Created EMPLOYEE: rm_karthik',                   DATE_SUB(NOW(), INTERVAL 165 DAY));

-- ==========================================================
-- CLEANUP & VERIFY
-- ==========================================================
SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

SELECT 'Dealers'        AS entity, COUNT(*) AS total FROM dealers
UNION ALL SELECT 'Users',          COUNT(*) FROM users
UNION ALL SELECT 'Employees',      COUNT(*) FROM employees
UNION ALL SELECT 'Customers',      COUNT(*) FROM customers
UNION ALL SELECT 'Customer Leads', COUNT(*) FROM customer_leads
UNION ALL SELECT 'Vehicle Models', COUNT(*) FROM vehicle_models
UNION ALL SELECT 'Inventory',      COUNT(*) FROM inventory
UNION ALL SELECT 'Sales Orders',   COUNT(*) FROM sales_orders
UNION ALL SELECT 'Payments',       COUNT(*) FROM payments
UNION ALL SELECT 'Audit Logs',     COUNT(*) FROM audit_logs;
