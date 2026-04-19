package com.hyundai.dms.controller;

import com.hyundai.dms.dto.AuthDto;
import com.hyundai.dms.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.hyundai.dms.repository.UserRepository userRepository;
    private final com.hyundai.dms.repository.RoleRepository roleRepository;
    private final com.hyundai.dms.repository.DealerRepository dealerRepository;
    private final com.hyundai.dms.repository.EmployeeRepository employeeRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.hyundai.dms.service.AuditService auditService;
    private final org.springframework.context.ApplicationContext applicationContext;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        String strRole = request.getRole() == null || request.getRole().isEmpty() ? "ROLE_EMPLOYEE" : request.getRole();
        Long assignedDealerId = request.getDealerId();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (assignedDealerId == null && auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            com.hyundai.dms.entity.User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null && currentUser.getDealerId() != null) {
                assignedDealerId = currentUser.getDealerId();
            }
        }

        // 🛠️ AUTO-DEALER CREATION: If a new dealer registers without an ID, give them their own silo
        if ("ROLE_DEALER".equals(strRole) && assignedDealerId == null) {
            String dealerName = (request.getFullName() != null ? request.getFullName() : request.getUsername()) + " Hyundai";
            com.hyundai.dms.entity.Dealer dEntity = com.hyundai.dms.entity.Dealer.builder()
                    .name(dealerName)
                    .isActive(true)
                    .registeredNumber("REG-" + System.currentTimeMillis() % 10000)
                    .build();
            com.hyundai.dms.entity.Dealer newDealer = dealerRepository.save(dEntity);
            assignedDealerId = newDealer.getId();
        }

        // Validate context
        if (assignedDealerId != null) {
            if (!dealerRepository.existsById(assignedDealerId)) {
                return ResponseEntity.badRequest().body("Error: Dealership context not found!");
            }
        } else if (!"ROLE_ADMIN".equals(strRole)) {
            return ResponseEntity.badRequest().body("Error: Non-admin users must be associated with a dealership.");
        }
        
        com.hyundai.dms.entity.Role userRole = roleRepository.findByName(strRole)
                .orElseGet(() -> {
                    com.hyundai.dms.entity.Role nr = com.hyundai.dms.entity.Role.builder().name(strRole).build();
                    return roleRepository.save(nr);
                });

        com.hyundai.dms.entity.User user = com.hyundai.dms.entity.User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .dealerId(assignedDealerId)
                .isActive(true)
                .roles(new java.util.HashSet<>(java.util.List.of(userRole)))
                .build();
        
        com.hyundai.dms.entity.User saved = userRepository.save(user);

        // 🟢 NEW: If user is an employee, create the matching Employee entity record
        if ("ROLE_EMPLOYEE".equals(strRole) && saved.getDealerId() != null) {
            com.hyundai.dms.entity.Employee employee = com.hyundai.dms.entity.Employee.builder()
                .user(saved)
                .dealerId(saved.getDealerId())
                .employeeCode("EMP-" + saved.getDealerId() + "-" + (1000 + saved.getId()))
                .designation("Staff")
                .hireDate(java.time.LocalDate.now())
                .build();
            employeeRepository.save(employee);
        }

        // Audit the creation
        try {
            auditService.logAction("CREATE", "USER", saved.getId(), "Created " + strRole.replace("ROLE_", "") + ": " + saved.getUsername());
        } catch (Exception ignored) {}

        return ResponseEntity.ok("User registered successfully!");
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@org.springframework.web.bind.annotation.RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(java.util.Collections.singletonMap("exists", exists));
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@org.springframework.web.bind.annotation.RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(java.util.Collections.singletonMap("exists", exists));
    }

    // End of checkEmail method

    @GetMapping("/dealers")
    public ResponseEntity<?> getActiveDealers() {
        var dealers = dealerRepository.findAll().stream()
                .filter(d -> d.getIsActive() != null && d.getIsActive())
                .map(d -> java.util.Map.of("id", d.getId(), "name", d.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dealers);
    }

    @GetMapping("/dev/reset-admin")
    public ResponseEntity<?> resetAdmin() {
        com.hyundai.dms.entity.User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            com.hyundai.dms.entity.Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(com.hyundai.dms.entity.Role.builder().name("ROLE_ADMIN").description("System Administrator").build()));
            admin = com.hyundai.dms.entity.User.builder()
                .username("admin").email("admin@hyundai.com").fullName("Super Admin")
                .passwordHash(passwordEncoder.encode("Admin@1234")).isActive(true)
                .roles(new java.util.HashSet<>(java.util.List.of(adminRole))).build();
            userRepository.save(admin);
            return ResponseEntity.ok("Admin created. Username: admin | Password: Admin@1234");
        }
        admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
        admin.setIsActive(true);
        admin.setFailedAttempts(0);
        admin.setAccountLocked(false);
        admin.setLockTime(null);
        userRepository.save(admin);
        return ResponseEntity.ok("Admin reset. Username: admin | Password: Admin@1234");
    }

    @GetMapping("/dev/fix-db")
    public ResponseEntity<?> fixDb() {
        try {
            javax.sql.DataSource ds = applicationContext.getBean(javax.sql.DataSource.class);
            try (java.sql.Connection conn = ds.getConnection(); java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("ALTER TABLE sales_orders MODIFY COLUMN lead_id BIGINT NULL");
                stmt.execute("ALTER TABLE sales_orders MODIFY COLUMN inventory_id BIGINT NULL");
                stmt.execute("ALTER TABLE sales_orders MODIFY COLUMN employee_id BIGINT NULL");
                stmt.execute("ALTER TABLE sales_orders MODIFY COLUMN total_amount DECIMAL(15,2) NULL");
                stmt.execute("ALTER TABLE sales_orders MODIFY COLUMN booking_amount DECIMAL(15,2) NULL");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                // Activate all dealers and users
                stmt.execute("UPDATE dealers SET is_active = 1 WHERE id > 0");
                stmt.execute("UPDATE users SET is_active = 1 WHERE id > 0");
                stmt.execute("UPDATE customers SET is_active = 1 WHERE id > 0");
            }
            return ResponseEntity.ok("DB fixed! All dealers, users, and core data activated.");
        } catch (Exception e) {
            return ResponseEntity.ok("Partial fix or already done: " + e.getMessage());
        }
    }

    @GetMapping("/dev/load-kiran-data")
    public ResponseEntity<?> loadKiranData() {
        java.util.List<String> results = new java.util.ArrayList<>();
        try {
            javax.sql.DataSource ds = applicationContext.getBean(javax.sql.DataSource.class);
            try (java.sql.Connection conn = ds.getConnection(); java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("SET SQL_SAFE_UPDATES = 0");

                // Clean up previous Kiran data to avoid duplicates
                stmt.execute("DELETE FROM payments WHERE sales_order_id BETWEEN 501 AND 530");
                stmt.execute("DELETE FROM sales_orders WHERE id BETWEEN 501 AND 530");
                stmt.execute("DELETE FROM customers WHERE id BETWEEN 501 AND 530");
                stmt.execute("DELETE FROM employees WHERE id BETWEEN 501 AND 515");
                stmt.execute("DELETE FROM user_roles WHERE user_id BETWEEN 501 AND 515");
                stmt.execute("DELETE FROM users WHERE id BETWEEN 501 AND 515");
                stmt.execute("DELETE FROM vehicles WHERE id BETWEEN 501 AND 525");
                stmt.execute("DELETE FROM departments WHERE id BETWEEN 50 AND 53");
                stmt.execute("DELETE FROM branches WHERE id BETWEEN 50 AND 51");
                results.add("Cleaned up old Kiran data");

                // Ensure Kiran Motors dealer exists with id=2
                stmt.execute("INSERT IGNORE INTO dealers (id, name, registered_number, contact_email, contact_phone, address, is_active) VALUES (2, 'Kiran Motors', 'REG-KIRAN-001', 'kiran@hyundai.com', '+91-9876543200', 'Koramangala, Bangalore', 1)");
                stmt.execute("UPDATE dealers SET is_active = 1 WHERE id = 2");

                // Branches & Departments
                stmt.execute("INSERT INTO branches (id, dealer_id, name, location) VALUES (50, 2, 'Kiran Motors Main', 'Koramangala, Bangalore') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                stmt.execute("INSERT INTO branches (id, dealer_id, name, location) VALUES (51, 2, 'Kiran Motors North', 'Hebbal, Bangalore') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                stmt.execute("INSERT INTO departments (id, branch_id, name) VALUES (50, 50, 'Sales') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                stmt.execute("INSERT INTO departments (id, branch_id, name) VALUES (51, 50, 'Service') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                stmt.execute("INSERT INTO departments (id, branch_id, name) VALUES (52, 50, 'Finance') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                stmt.execute("INSERT INTO departments (id, branch_id, name) VALUES (53, 51, 'Sales') ON DUPLICATE KEY UPDATE name=VALUES(name)");
                results.add("Loaded branches & departments");

                // Employee Users (password hash for 'password123')
                String empHash = passwordEncoder.encode("password123");
                String[] empInserts = {
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (501, 'km_arjun', '%s', 'arjun@kiran.com', 'Arjun Sharma', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (502, 'km_priya', '%s', 'priya@kiran.com', 'Priya Nair', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (503, 'km_rahul', '%s', 'rahul@kiran.com', 'Rahul Verma', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (504, 'km_sneha', '%s', 'sneha@kiran.com', 'Sneha Pillai', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (505, 'km_karthik', '%s', 'karthik@kiran.com', 'Karthik Rajan', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (506, 'km_divya', '%s', 'divya@kiran.com', 'Divya Menon', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (507, 'km_vikram', '%s', 'vikram@kiran.com', 'Vikram Singh', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (508, 'km_ananya', '%s', 'ananya@kiran.com', 'Ananya Iyer', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (509, 'km_suresh', '%s', 'suresh@kiran.com', 'Suresh Kumar', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (510, 'km_meera', '%s', 'meera@kiran.com', 'Meera Krishnan', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (511, 'km_aditya', '%s', 'aditya@kiran.com', 'Aditya Patel', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (512, 'km_lakshmi', '%s', 'lakshmi@kiran.com', 'Lakshmi Devi', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (513, 'km_rohan', '%s', 'rohan@kiran.com', 'Rohan Mehta', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (514, 'km_pooja', '%s', 'pooja@kiran.com', 'Pooja Sharma', 1, 2)", empHash),
                    String.format("INSERT INTO users (id, username, password_hash, email, full_name, is_active, dealer_id) VALUES (515, 'km_nikhil', '%s', 'nikhil@kiran.com', 'Nikhil Chandra', 1, 2)", empHash)
                };
                for (String sql : empInserts) { stmt.execute(sql); }
                results.add("Loaded 15 employee users");

                // User roles (role_id=3 = ROLE_EMPLOYEE)
                for (int i = 501; i <= 515; i++) {
                    stmt.execute("INSERT INTO user_roles (user_id, role_id) VALUES (" + i + ", 3)");
                }
                results.add("Assigned ROLE_EMPLOYEE to all");

                // Employees
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (501, 501, 50, 'EMP-2-501', 'Senior Sales Executive', '2021-06-01', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (502, 502, 50, 'EMP-2-502', 'Sales Executive', '2022-01-15', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (503, 503, 50, 'EMP-2-503', 'Sales Executive', '2022-04-10', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (504, 504, 50, 'EMP-2-504', 'Sales Consultant', '2022-08-20', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (505, 505, 53, 'EMP-2-505', 'Senior Sales Executive', '2021-03-05', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (506, 506, 51, 'EMP-2-506', 'Service Advisor', '2022-11-12', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (507, 507, 51, 'EMP-2-507', 'Service Technician', '2023-01-28', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (508, 508, 53, 'EMP-2-508', 'Sales Consultant', '2023-03-15', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (509, 509, 52, 'EMP-2-509', 'Finance Manager', '2020-09-01', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (510, 510, 50, 'EMP-2-510', 'Sales Executive', '2023-07-08', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (511, 511, 53, 'EMP-2-511', 'Sales Executive', '2023-09-14', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (512, 512, 52, 'EMP-2-512', 'Finance Executive', '2022-12-01', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (513, 513, 50, 'EMP-2-513', 'Sales Executive', '2024-01-10', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (514, 514, 53, 'EMP-2-514', 'Sales Consultant', '2024-02-20', 2)");
                stmt.execute("INSERT INTO employees (id, user_id, department_id, employee_code, designation, hire_date, dealer_id) VALUES (515, 515, 50, 'EMP-2-515', 'Junior Sales Executive', '2024-03-05', 2)");
                results.add("Loaded 15 employee records");

                // Vehicles (25)
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (501, 'Creta', 'Hyundai', 'E Petrol MT', 2024, 1099000.00, 7, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (502, 'Creta', 'Hyundai', 'S Petrol MT', 2024, 1199000.00, 5, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (503, 'Creta', 'Hyundai', 'SX Petrol AT', 2024, 1499000.00, 4, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (504, 'Creta', 'Hyundai', 'SX(O) Diesel AT', 2024, 1799000.00, 2, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (505, 'Venue', 'Hyundai', 'E Petrol MT', 2024, 794000.00, 9, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (506, 'Venue', 'Hyundai', 'S Petrol MT', 2024, 894000.00, 6, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (507, 'Venue', 'Hyundai', 'SX Turbo DCT', 2024, 1094000.00, 3, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (508, 'Verna', 'Hyundai', 'EX Petrol MT', 2024, 1099000.00, 5, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (509, 'Verna', 'Hyundai', 'S Petrol IVT', 2024, 1299000.00, 4, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (510, 'Verna', 'Hyundai', 'SX Turbo DCT', 2024, 1499000.00, 2, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (511, 'i20', 'Hyundai', 'Era Petrol MT', 2024, 699000.00, 8, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (512, 'i20', 'Hyundai', 'Magna Petrol MT', 2024, 799000.00, 6, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (513, 'i20', 'Hyundai', 'Sportz Petrol IVT', 2024, 949000.00, 4, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (514, 'i20', 'Hyundai', 'Asta Turbo DCT', 2024, 1099000.00, 2, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (515, 'Tucson', 'Hyundai', 'Platinum Petrol AT', 2024, 2799000.00, 2, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (516, 'Tucson', 'Hyundai', 'Signature Diesel AT', 2024, 2999000.00, 1, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (517, 'Exter', 'Hyundai', 'EX Petrol MT', 2024, 599000.00, 11, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (518, 'Exter', 'Hyundai', 'S Petrol MT', 2024, 699000.00, 7, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (519, 'Exter', 'Hyundai', 'SX Petrol AMT', 2024, 799000.00, 4, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (520, 'Exter', 'Hyundai', 'SX(O) CNG MT', 2024, 849000.00, 3, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (521, 'Alcazar', 'Hyundai', 'Platinum Petrol AT', 2024, 2099000.00, 2, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (522, 'Alcazar', 'Hyundai', 'Signature Diesel AT', 2024, 2299000.00, 1, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (523, 'Aura', 'Hyundai', 'S Petrol MT', 2024, 799000.00, 5, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (524, 'Aura', 'Hyundai', 'SX Petrol AMT', 2024, 899000.00, 3, 2)");
                stmt.execute("INSERT INTO vehicles (id, model_name, brand, variant, year, base_price, stock, dealer_id) VALUES (525, 'Ioniq 5', 'Hyundai', 'Standard Range', 2024, 4499000.00, 1, 2)");
                results.add("Loaded 25 vehicles");

                // Customers (30)
                String[] custInserts = {
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (501, 'Aarav', 'Mehta', 'aarav.km@gmail.com', '9880001001', '12 Koramangala, Bangalore', 'Interested in Creta SX', 'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 170 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (502, 'Bhavna', 'Sharma', 'bhavna.km@gmail.com', '9880001002', '34 Indiranagar, Bangalore', 'Looking for family SUV', 'NEW', 502, 2, DATE_SUB(NOW(), INTERVAL 165 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (503, 'Chetan', 'Patel', 'chetan.km@gmail.com', '9880001003', '56 HSR Layout, Bangalore', 'Test drive done for Venue', 'BOOKED', 503, 2, DATE_SUB(NOW(), INTERVAL 160 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (504, 'Deepika', 'Nair', 'deepika.km@gmail.com', '9880001004', '78 Jayanagar, Bangalore', 'Budget around 12L', 'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 155 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (505, 'Eshan', 'Gupta', 'eshan.km@gmail.com', '9880001005', '90 JP Nagar, Bangalore', 'Wants diesel variant', 'NEW', 505, 2, DATE_SUB(NOW(), INTERVAL 150 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (506, 'Farida', 'Khan', 'farida.km@gmail.com', '9880001006', '11 BTM Layout, Bangalore', 'Comparing with Nexon', 'LOST', 501, 2, DATE_SUB(NOW(), INTERVAL 145 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (507, 'Ganesh', 'Iyer', 'ganesh.km@gmail.com', '9880001007', '22 Banashankari, Bangalore', 'First time buyer', 'NEW', 502, 2, DATE_SUB(NOW(), INTERVAL 140 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (508, 'Harini', 'Reddy', 'harini.km@gmail.com', '9880001008', '33 Rajajinagar, Bangalore', 'Wants automatic', 'INTERESTED', 503, 2, DATE_SUB(NOW(), INTERVAL 135 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (509, 'Ishaan', 'Verma', 'ishaan.km@gmail.com', '9880001009', '44 Malleshwaram, Bangalore', 'Corporate purchase', 'BOOKED', 504, 2, DATE_SUB(NOW(), INTERVAL 130 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (510, 'Jaya', 'Krishnan', 'jaya.km@gmail.com', '9880001010', '55 Sadashivanagar', 'Exchange offer discussed', 'INTERESTED', 505, 2, DATE_SUB(NOW(), INTERVAL 125 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (511, 'Kiran', 'Bose', 'kiran.km@gmail.com', '9880001011', '66 Hebbal, Bangalore', 'Wants i20 Asta', 'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 120 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (512, 'Lavanya', 'Pillai', 'lavanya.km@gmail.com', '9880001012', '77 Yelahanka, Bangalore', 'Finance pre-approved', 'BOOKED', 502, 2, DATE_SUB(NOW(), INTERVAL 115 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (513, 'Manoj', 'Singh', 'manoj.km@gmail.com', '9880001013', '88 Devanahalli, Bangalore', 'Wants white color only', 'NEW', 503, 2, DATE_SUB(NOW(), INTERVAL 110 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (514, 'Nandita', 'Rao', 'nandita.km@gmail.com', '9880001014', '99 Whitefield, Bangalore', 'Interested in Tucson', 'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 105 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (515, 'Om', 'Prakash', 'om.km@gmail.com', '9880001015', '10 Marathahalli, Bangalore', 'Bought elsewhere', 'LOST', 505, 2, DATE_SUB(NOW(), INTERVAL 100 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (516, 'Pooja', 'Menon', 'pooja.km@gmail.com', '9880001016', '21 Sarjapur, Bangalore', 'Wants Verna SX Turbo', 'BOOKED', 501, 2, DATE_SUB(NOW(), INTERVAL 95 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (517, 'Qasim', 'Ali', 'qasim.km@gmail.com', '9880001017', '32 Electronic City', 'Looking for EV options', 'NEW', 502, 2, DATE_SUB(NOW(), INTERVAL 90 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (518, 'Riya', 'Desai', 'riya.km@gmail.com', '9880001018', '43 Bannerghatta, Bangalore', 'Wants Exter CNG', 'INTERESTED', 503, 2, DATE_SUB(NOW(), INTERVAL 85 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (519, 'Sanjay', 'Kumar', 'sanjay.km@gmail.com', '9880001019', '54 Kanakapura, Bangalore', 'Fleet purchase inquiry', 'INTERESTED', 504, 2, DATE_SUB(NOW(), INTERVAL 80 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (520, 'Tanvi', 'Shah', 'tanvi.km@gmail.com', '9880001020', '65 Mysore Road, Bangalore', 'Wants sunroof variant', 'BOOKED', 505, 2, DATE_SUB(NOW(), INTERVAL 75 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (521, 'Uday', 'Rajan', 'uday.km@gmail.com', '9880001021', '76 Tumkur Road, Bangalore', 'Budget 8-10L', 'NEW', 501, 2, DATE_SUB(NOW(), INTERVAL 70 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (522, 'Vandana', 'Tiwari', 'vandana.km@gmail.com', '9880001022', '87 Peenya, Bangalore', 'Interested in i20 Sportz', 'INTERESTED', 502, 2, DATE_SUB(NOW(), INTERVAL 65 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (523, 'Wasim', 'Ansari', 'wasim.km@gmail.com', '9880001023', '98 Yeshwanthpur', 'Needs 7-seater', 'LOST', 503, 2, DATE_SUB(NOW(), INTERVAL 60 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (524, 'Xena', 'Thomas', 'xena.km@gmail.com', '9880001024', '19 Nagarbhavi, Bangalore', 'Wants Creta diesel', 'BOOKED', 504, 2, DATE_SUB(NOW(), INTERVAL 55 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (525, 'Yogesh', 'Patil', 'yogesh.km@gmail.com', '9880001025', '28 Vijayanagar, Bangalore', 'First car purchase', 'NEW', 505, 2, DATE_SUB(NOW(), INTERVAL 50 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (526, 'Zara', 'Hussain', 'zara.km@gmail.com', '9880001026', '37 Basaveshwara Nagar', 'Wants Venue S', 'INTERESTED', 501, 2, DATE_SUB(NOW(), INTERVAL 45 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (527, 'Amit', 'Joshi', 'amit.km@gmail.com', '9880001027', '46 Mathikere, Bangalore', 'Comparing Venue vs Exter', 'INTERESTED', 502, 2, DATE_SUB(NOW(), INTERVAL 40 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (528, 'Bindu', 'Nambiar', 'bindu.km@gmail.com', '9880001028', '55 RT Nagar, Bangalore', 'Wants finance option', 'BOOKED', 503, 2, DATE_SUB(NOW(), INTERVAL 35 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (529, 'Chirag', 'Malhotra', 'chirag.km@gmail.com', '9880001029', '64 Banaswadi, Bangalore', 'Interested in Creta E', 'NEW', 504, 2, DATE_SUB(NOW(), INTERVAL 20 DAY))",
                    "INSERT INTO customers (id, first_name, last_name, email, phone, address, notes, status, assigned_employee_id, dealer_id, created_at) VALUES (530, 'Disha', 'Kapoor', 'disha.km@gmail.com', '9880001030', '73 Kammanahalli', 'Wants Ioniq 5', 'INTERESTED', 505, 2, DATE_SUB(NOW(), INTERVAL 10 DAY))"
                };
                for (String sql : custInserts) { stmt.execute(sql); }
                results.add("Loaded 30 customers");

                // Sales Orders (30)
                String[] salesInserts = {
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (501, 1, 5001, 501, 503, 505, 2, 894000.00, 20000.00, 874000.00, 874000.00, 25000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 160 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (502, 1, 5002, 502, 509, 503, 2, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 150 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (503, 1, 5003, 503, 512, 511, 2, 699000.00, 10000.00, 689000.00, 689000.00, 20000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 140 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (504, 1, 5004, 504, 516, 509, 2, 1299000.00, 25000.00, 1274000.00, 1274000.00, 40000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 130 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (505, 1, 5005, 505, 520, 503, 2, 1499000.00, 35000.00, 1464000.00, 1464000.00, 50000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 120 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (506, 1, 5006, 501, 524, 504, 2, 1799000.00, 50000.00, 1749000.00, 1749000.00, 75000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 110 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (507, 1, 5007, 502, 528, 508, 2, 1099000.00, 20000.00, 1079000.00, 1079000.00, 30000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 100 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (508, 1, 5008, 503, 501, 503, 2, 1499000.00, 30000.00, 1469000.00, 1469000.00, 50000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 90 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (509, 1, 5009, 504, 504, 506, 2, 894000.00, 15000.00, 879000.00, 879000.00, 25000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 80 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (510, 1, 5010, 505, 508, 513, 2, 949000.00, 20000.00, 929000.00, 929000.00, 25000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 70 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (511, 1, 5011, 501, 511, 512, 2, 799000.00, 10000.00, 789000.00, 789000.00, 20000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 60 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (512, 1, 5012, 502, 514, 515, 2, 2799000.00, 75000.00, 2724000.00, 2724000.00, 100000.00, 'INVOICED', DATE_SUB(NOW(), INTERVAL 50 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (513, 1, 5013, 503, 518, 519, 2, 799000.00, 10000.00, 789000.00, 789000.00, 20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 45 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (514, 1, 5014, 504, 519, 505, 2, 794000.00, 5000.00, 789000.00, 789000.00, 20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 42 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (515, 1, 5015, 505, 522, 513, 2, 949000.00, 15000.00, 934000.00, 934000.00, 25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 38 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (516, 1, 5016, 501, 526, 506, 2, 894000.00, 10000.00, 884000.00, 884000.00, 25000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 35 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (517, 1, 5017, 502, 527, 517, 2, 599000.00, 0.00, 599000.00, 599000.00, 15000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 30 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (518, 1, 5018, 503, 529, 501, 2, 1099000.00, 15000.00, 1084000.00, 1084000.00, 30000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 25 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (519, 1, 5019, 504, 530, 525, 2, 4499000.00, 100000.00, 4399000.00, 4399000.00, 150000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 20 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (520, 1, 5020, 505, 502, 518, 2, 699000.00, 5000.00, 694000.00, 694000.00, 20000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 18 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (521, 1, 5021, 501, 505, 504, 2, 1799000.00, 40000.00, 1759000.00, 1759000.00, 60000.00, 'CONFIRMED', DATE_SUB(NOW(), INTERVAL 15 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (522, 1, 5022, 502, 507, 511, 2, 699000.00, 0.00, 699000.00, 699000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 12 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (523, 1, 5023, 503, 513, 501, 2, 1099000.00, 0.00, 1099000.00, 1099000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 10 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (524, 1, 5024, 504, 517, 517, 2, 599000.00, 0.00, 599000.00, 599000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 8 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (525, 1, 5025, 505, 521, 509, 2, 1299000.00, 0.00, 1299000.00, 1299000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 5 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (526, 1, 5026, 501, 525, 505, 2, 794000.00, 0.00, 794000.00, 794000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 3 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (527, 1, 5027, 502, 529, 503, 2, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (528, 1, 5028, 503, 506, 507, 2, 1094000.00, 0.00, 1094000.00, 1094000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 145 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (529, 1, 5029, 504, 515, 516, 2, 2999000.00, 0.00, 2999000.00, 2999000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 100 DAY))",
                    "INSERT INTO sales_orders (id, lead_id, inventory_id, employee_id, customer_id, vehicle_id, dealer_id, price, discount, final_amount, total_amount, booking_amount, status, created_at) VALUES (530, 1, 5030, 505, 523, 510, 2, 1499000.00, 0.00, 1499000.00, 1499000.00, 0.00, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 55 DAY))"
                };
                for (String sql : salesInserts) { stmt.execute(sql); }
                results.add("Loaded 30 sales orders");

                // Payments (12 for invoiced orders)
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (501, 874000.00, 'BANK_TRANSFER', 'TXN-KM-501', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 159 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (502, 1469000.00, 'FINANCE', 'TXN-KM-502', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 149 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (503, 689000.00, 'CASH', 'TXN-KM-503', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 139 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (504, 1274000.00, 'BANK_TRANSFER', 'TXN-KM-504', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 129 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (505, 1464000.00, 'FINANCE', 'TXN-KM-505', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 119 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (506, 1749000.00, 'FINANCE', 'TXN-KM-506', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 109 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (507, 1079000.00, 'BANK_TRANSFER', 'TXN-KM-507', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 99 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (508, 1469000.00, 'FINANCE', 'TXN-KM-508', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 89 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (509, 879000.00, 'CASH', 'TXN-KM-509', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 79 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (510, 929000.00, 'CARD', 'TXN-KM-510', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 69 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (511, 789000.00, 'BANK_TRANSFER', 'TXN-KM-511', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 59 DAY))");
                stmt.execute("INSERT INTO payments (sales_order_id, amount, payment_mode, transaction_ref, payment_status, payment_date) VALUES (512, 2724000.00, 'FINANCE', 'TXN-KM-512', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 49 DAY))");
                results.add("Loaded 12 payments");

                stmt.execute("SET SQL_SAFE_UPDATES = 1");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

                // Verify counts
                java.sql.ResultSet rs = stmt.executeQuery("SELECT (SELECT COUNT(*) FROM employees WHERE dealer_id=2) as emp, (SELECT COUNT(*) FROM vehicles WHERE dealer_id=2) as veh, (SELECT COUNT(*) FROM customers WHERE dealer_id=2) as cust, (SELECT COUNT(*) FROM sales_orders WHERE dealer_id=2) as sales");
                if (rs.next()) {
                    results.add("VERIFIED — Employees: " + rs.getInt("emp") + ", Vehicles: " + rs.getInt("veh") + ", Customers: " + rs.getInt("cust") + ", Sales: " + rs.getInt("sales"));
                }
            }
            return ResponseEntity.ok(java.util.Map.of("status", "Kiran Motors data loaded!", "details", results));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dev/unlock/{username}")
    public ResponseEntity<?> unlockUser(@PathVariable String username) {
        com.hyundai.dms.entity.User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        user.setIsActive(true);
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
        return ResponseEntity.ok("User unlocked: " + username);
    }

    @GetMapping("/dev/reset-accounts")
    public ResponseEntity<?> resetAllAccounts() {
        java.util.List<String> results = new java.util.ArrayList<>();

        // 1. Reset admin account
        com.hyundai.dms.entity.User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            com.hyundai.dms.entity.Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(com.hyundai.dms.entity.Role.builder().name("ROLE_ADMIN").description("System Administrator").build()));
            admin = com.hyundai.dms.entity.User.builder()
                .username("admin").email("admin@hyundai.com").fullName("Super Admin")
                .passwordHash(passwordEncoder.encode("Admin@1234")).isActive(true)
                .roles(new java.util.HashSet<>(java.util.List.of(adminRole))).build();
            userRepository.save(admin);
            results.add("admin: CREATED (Admin@1234)");
        } else {
            admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
            admin.setIsActive(true);
            admin.setFailedAttempts(0);
            admin.setAccountLocked(false);
            admin.setLockTime(null);
            userRepository.save(admin);
            results.add("admin: RESET (Admin@1234) — unlocked & activated");
        }

        // 2. Ensure dealer_id=2 (Kiran Motors) exists and is active
        com.hyundai.dms.entity.Dealer kiranDealer = dealerRepository.findById(2L).orElse(null);
        if (kiranDealer == null) {
            kiranDealer = com.hyundai.dms.entity.Dealer.builder()
                .name("Kiran Motors")
                .registeredNumber("REG-KIRAN-001")
                .contactEmail("kiran@hyundai.com")
                .contactPhone("+91-9876543200")
                .address("Koramangala, Bangalore")
                .isActive(true)
                .build();
            kiranDealer = dealerRepository.save(kiranDealer);
            results.add("Kiran Motors dealer: CREATED (id=" + kiranDealer.getId() + ")");
        } else {
            kiranDealer.setIsActive(true);
            dealerRepository.save(kiranDealer);
            results.add("Kiran Motors dealer: ACTIVATED (id=2)");
        }

        // 3. Reset kiran_motors user account
        com.hyundai.dms.entity.User kiran = userRepository.findByUsername("kiran_motors").orElse(null);
        if (kiran == null) {
            com.hyundai.dms.entity.Role dealerRole = roleRepository.findByName("ROLE_DEALER")
                .orElseGet(() -> roleRepository.save(com.hyundai.dms.entity.Role.builder().name("ROLE_DEALER").description("Dealership Manager").build()));
            kiran = com.hyundai.dms.entity.User.builder()
                .username("kiran_motors")
                .email("kiran_motors@hyundai.com")
                .fullName("Kiran Motors")
                .passwordHash(passwordEncoder.encode("Kiran@2024"))
                .dealerId(kiranDealer.getId())
                .isActive(true)
                .roles(new java.util.HashSet<>(java.util.List.of(dealerRole)))
                .build();
            userRepository.save(kiran);
            results.add("kiran_motors: CREATED (Kiran@2024, dealer_id=" + kiranDealer.getId() + ")");
        } else {
            kiran.setPasswordHash(passwordEncoder.encode("Kiran@2024"));
            kiran.setIsActive(true);
            kiran.setFailedAttempts(0);
            kiran.setAccountLocked(false);
            kiran.setLockTime(null);
            kiran.setDealerId(kiranDealer.getId());
            userRepository.save(kiran);
            results.add("kiran_motors: RESET (Kiran@2024) — unlocked & activated, dealer_id=" + kiranDealer.getId());
        }

        // 4. Also activate all dealers to be safe
        dealerRepository.findAll().forEach(d -> {
            if (d.getIsActive() == null || !d.getIsActive()) {
                d.setIsActive(true);
                dealerRepository.save(d);
            }
        });
        results.add("All dealers: ACTIVATED");

        return ResponseEntity.ok(java.util.Map.of(
            "status", "All accounts reset successfully",
            "details", results
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> authenticateUser(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        try {
            // Pre-check: account lock and expiry BEFORE attempting auth
            com.hyundai.dms.entity.User preCheck = userRepository.findByUsername(loginRequest.getUsername()).orElse(null);
            if (preCheck != null) {
                // Check expiry
                if (preCheck.getAccountExpiryDate() != null && preCheck.getAccountExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                    throw new org.springframework.security.authentication.CredentialsExpiredException(
                        "Your account has expired. Please contact admin.");
                }
                
                // Check if dealer is deactivated
                if (preCheck.getDealerId() != null) {
                    com.hyundai.dms.entity.Dealer d = dealerRepository.findById(preCheck.getDealerId()).orElse(null);
                    if (d != null && (d.getIsActive() != null && !d.getIsActive())) {
                        throw new org.springframework.security.authentication.DisabledException(
                            "Dealer deactivated contact admin ok");
                    }
                }

                // Check lock — auto-unlock after 30 min
                if (Boolean.TRUE.equals(preCheck.getAccountLocked())) {
                    if (preCheck.getLockTime() != null && preCheck.getLockTime().plusMinutes(30).isBefore(java.time.LocalDateTime.now())) {
                        // Auto-unlock
                        preCheck.setAccountLocked(false);
                        preCheck.setFailedAttempts(0);
                        preCheck.setLockTime(null);
                        userRepository.save(preCheck);
                    } else {
                        throw new org.springframework.security.authentication.LockedException(
                            "Your account is locked due to multiple failed login attempts. Try again after 30 minutes.");
                    }
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            List<String> roles = authorities.stream().filter(a -> a.startsWith("ROLE_")).collect(Collectors.toList());
            List<String> permissions = authorities.stream().filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toList());

            com.hyundai.dms.entity.User dbUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            Long dealerId = dbUser != null ? dbUser.getDealerId() : null;
            String dealerName = null;
            if (dealerId != null) {
                dealerName = dealerRepository.findById(dealerId).map(com.hyundai.dms.entity.Dealer::getName).orElse(null);
            }

            // Reset failed attempts on successful login
            if (dbUser != null) {
                dbUser.setFailedAttempts(0);
                dbUser.setAccountLocked(false);
                dbUser.setLockTime(null);
                userRepository.save(dbUser);
                try { auditService.logAction("LOGIN", "SYSTEM", dbUser.getId(), "User logged in: " + dbUser.getUsername()); } catch (Exception ignored) {}
            }

            return ResponseEntity.ok(AuthDto.AuthResponse.builder()
                    .token(jwt).username(userDetails.getUsername())
                    .roles(roles).permissions(permissions)
                    .dealerId(dealerId).dealerName(dealerName).build());

        } catch (org.springframework.security.authentication.LockedException |
                 org.springframework.security.authentication.CredentialsExpiredException e) {
            throw e;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Increment failed attempts
            userRepository.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
                user.setFailedAttempts(attempts);
                if (attempts >= 5) {
                    user.setAccountLocked(true);
                    user.setLockTime(java.time.LocalDateTime.now());
                }
                userRepository.save(user);
                try { auditService.logAction("FAILED_LOGIN", "SYSTEM", user.getId(), "Failed login attempt " + attempts + " for: " + user.getUsername()); } catch (Exception ignored) {}
            });
            throw e;
        }
    }
}
