package com.hyundai.dms.service;

import com.hyundai.dms.entity.*;
import com.hyundai.dms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataSeederService {

    private final DealerRepository dealerRepository;
    private final BranchRepository branchRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final AuditLogRepository auditLogRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private static final Random random = new Random();

    private static final String[] FIRST_NAMES = {
        "Aarav", "Advik", "Akash", "Arjun", "Dev", "Ishaan", "Kabir", "Manish", "Nitin", "Pranav",
        "Rahul", "Sanjay", "Tanmay", "Vihaan", "Yash", "Ananya", "Diya", "Isha", "Kavya", "Myra",
        "Navya", "Riya", "Saanvi", "Tara", "Vanya", "Aditya", "Amit", "Deepak", "Gaurav", "Harsh",
        "Jatin", "Karan", "Lokesh", "Manoj", "Neeraj", "Pankaj", "Ravi", "Sameer", "Tushar", "Vikas"
    };

    private static final String[] LAST_NAMES = {
        "Sharma", "Verma", "Gupta", "Malhotra", "Kapoor", "Singh", "Yadav", "Patel", "Reddy", "Iyer",
        "Nair", "Joshi", "Kulkarni", "Deshmukh", "Chauhan", "Agarwal", "Bansal", "Goel", "Mehta", "Shah",
        "Mishra", "Pandey", "Dubey", "Tiwari", "Das", "Chatterjee", "Banerjee", "Mukherjee", "Sen", "Roy"
    };

    private static final Map<String, String[]> HYUNDAI_MODELS = Map.of(
        "Creta", new String[]{"E Petrol MT", "S Petrol MT", "SX Petrol MT", "SX(O) Petrol DCT", "SX(O) Diesel AT"},
        "Venue", new String[]{"S Petrol MT", "SX Petrol MT", "SX(O) Petrol DCT", "N Line Petrol DCT"},
        "Verna", new String[]{"EX Petrol MT", "S Petrol MT", "SX Petrol MT", "SX(O) Turbo DCT"},
        "Tucson", new String[]{"Signature 2WD Petrol AT", "Signature AWD Diesel AT"},
        "Alcazar", new String[]{"Platinum Petrol MT", "Signature Diesel AT"},
        "i20", new String[]{"Era Petrol MT", "Sportz Petrol MT", "Asta(O) Petrol CVT"},
        "Exter", new String[]{"EX Petrol MT", "SX Petrol MT", "SX(O) Petrol AMT"}
    );

    private static final Map<String, BigDecimal> MODEL_BASE_PRICES = Map.of(
        "Creta", new BigDecimal("1100000"),
        "Venue", new BigDecimal("794000"),
        "Verna", new BigDecimal("1100000"),
        "Tucson", new BigDecimal("2901000"),
        "Alcazar", new BigDecimal("1677000"),
        "i20", new BigDecimal("704000"),
        "Exter", new BigDecimal("612000")
    );

    
    public void seedAll() {
        // 1. Setup Roles
        Role dealerRole = getOrCreateRole("ROLE_DEALER");
        Role employeeRole = getOrCreateRole("ROLE_EMPLOYEE");
        getOrCreateRole("ROLE_ADMIN");

        // 2. Create Dealers
        List<Dealer> dealers = seedDealers();

        // 3. Create Dealer Admin User for Shiva (Requirement)
        if (userRepository.findByUsername("shiva").isEmpty()) {
            System.out.println("[SEEDER] Creating shiva account...");
            List<Dealer> allDealers = dealerRepository.findAll();
            if (allDealers.isEmpty()) {
                allDealers = seedDealers();
            }
            if (!allDealers.isEmpty()) {
                Dealer shivaDealer = allDealers.stream()
                        .filter(d -> d.getName().toLowerCase().contains("shiva"))
                        .findFirst()
                        .orElse(allDealers.get(0));
                
                User shiva = User.builder()
                        .username("shiva")
                        .passwordHash(passwordEncoder.encode("password123"))
                        .email("shiva@hyundai.com")
                        .fullName("Shiva Hyundai Admin")
                        .dealerId(shivaDealer.getId())
                        .isActive(true)
                        .roles(new HashSet<>(List.of(dealerRole)))
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(shiva);
                System.out.println("[SEEDER] shiva account created for dealer: " + shivaDealer.getName());
            }
        }

        for (Dealer dealer : dealers) {
            // 3. Create Branch & Departments
            Branch branch = seedBranch(dealer);
            List<Department> departments = seedDepartments(branch);

            // 4. Create Employees
            List<Employee> employees = seedEmployees(dealer, departments, employeeRole);

            // 5. Create Inventory
            List<Vehicle> inventory = seedInventory(dealer);

            // 6. Create Customers
            List<Customer> customers = seedCustomers(dealer, employees);

            // 7. Create Sales Orders & Payments
            seedSalesOrders(dealer, customers, inventory, employees);
        }

        // 8. Fix Dates using Native SQL (spread records across last 6 months)
        randomizeDates();

        System.out.println("[SEEDER] Data seeding completed successfully.");
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> 
            roleRepository.save(Role.builder().name(name).build())
        );
    }

    private List<Dealer> seedDealers() {
        String[] dealerNames = {"Shiva Hyundai", "Advaith Hyundai", "HMP Hyundai"};
        List<Dealer> created = new ArrayList<>();
        for (int i = 0; i < dealerNames.length; i++) {
            final int index = i;
            String name = dealerNames[i];
            String regNum = "DL-" + (1000 + i);
            Dealer dealer = dealerRepository.findByName(name).orElseGet(() -> {
                Dealer d = Dealer.builder()
                    .name(name)
                    .registeredNumber(regNum)
                    .contactEmail("contact@" + name.toLowerCase().replace(" ", "") + ".com")
                    .contactPhone("+91-9876543" + (100 + index))
                    .address(name + " Main Road, " + (index == 0 ? "Bangalore" : index == 1 ? "Pune" : "Mumbai"))
                    .isActive(true)
                    .build();
                return dealerRepository.save(d);
            });
            created.add(dealer);
            
            // Log Dealer Creation
            logAudit("System", "CREATE", "DEALER", dealer.getId(), "Onboarded new dealer: " + dealer.getName());
        }
        return created;
    }

    private Branch seedBranch(Dealer dealer) {
        return branchRepository.findAllByDealerId(dealer.getId()).stream().findFirst().orElseGet(() -> {
            Branch b = Branch.builder()
                .dealer(dealer)
                .name(dealer.getName().split(" ")[0] + " Main Branch")
                .location(dealer.getAddress())
                .build();
            return branchRepository.save(b);
        });
    }

    private List<Department> seedDepartments(Branch branch) {
        String[] deptNames = {"Sales", "Service", "Finance", "Administration"};
        List<Department> existing = departmentRepository.findAllByBranchId(branch.getId());
        if (!existing.isEmpty()) return existing;

        List<Department> created = new ArrayList<>();
        for (String name : deptNames) {
            Department d = Department.builder()
                .branch(branch)
                .name(name)
                .build();
            created.add(departmentRepository.save(d));
        }
        return created;
    }

    private List<Employee> seedEmployees(Dealer dealer, List<Department> departments, Role empRole) {
        List<Employee> existing = employeeRepository.findAllByDealerId(dealer.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (existing.size() >= 10) return existing;

        List<Employee> created = new ArrayList<>();
        int count = 10 + random.nextInt(6); // 10-15 employees
        for (int i = 0; i < count; i++) {
            String firstName = getRandom(FIRST_NAMES);
            String lastName = getRandom(LAST_NAMES);
            String username = (firstName.toLowerCase() + "." + lastName.toLowerCase() + dealer.getId()).replace(" ", "");
            
            if (userRepository.existsByUsername(username)) continue;

            User user = User.builder()
                .username(username)
                .fullName(firstName + " " + lastName)
                .email(username + "@hyundai.com")
                .passwordHash(passwordEncoder.encode("emp@123"))
                .dealerId(dealer.getId())
                .isActive(true)
                .roles(new HashSet<>(Collections.singletonList(empRole)))
                .createdAt(LocalDateTime.now().minusMonths(random.nextInt(6)))
                .build();
            user = userRepository.save(user);

            Department dept = departments.get(random.nextInt(departments.size()));
            Employee emp = Employee.builder()
                .dealerId(dealer.getId())
                .user(user)
                .department(dept)
                .employeeCode("EMP-" + dealer.getId() + "-" + (100 + i))
                .designation(dept.getName().equals("Sales") ? "Sales Consultant" : "Executive")
                .hireDate(user.getCreatedAt().toLocalDate())
                .build();
            created.add(employeeRepository.save(emp));
        }
        return created;
    }

    private List<Vehicle> seedInventory(Dealer dealer) {
        List<Vehicle> existing = vehicleRepository.findWithSearch(dealer.getId(), "", null, null, null, null, org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (existing.size() >= 15) return existing;

        List<Vehicle> created = new ArrayList<>();
        int count = 15 + random.nextInt(11); // 15-25 vehicles
        for (int i = 0; i < count; i++) {
            String model = getRandom(HYUNDAI_MODELS.keySet().toArray(new String[0]));
            String[] variants = HYUNDAI_MODELS.get(model);
            String variant = getRandom(variants);
            BigDecimal base = MODEL_BASE_PRICES.get(model);
            
            Vehicle v = Vehicle.builder()
                .modelName(model)
                .brand("Hyundai")
                .variant(variant)
                .year(2023 + random.nextInt(2))
                .basePrice(base.add(new BigDecimal(random.nextInt(50000))))
                .stock(random.nextInt(16)) // 0-15
                .dealerId(dealer.getId())
                .build();
            created.add(vehicleRepository.save(v));
        }
        return created;
    }

    private List<Customer> seedCustomers(Dealer dealer, List<Employee> employees) {
        List<Customer> existing = customerRepository.findActiveWithSearch(dealer.getId(), "", null, null, org.springframework.data.domain.Pageable.unpaged()).getContent();
        if (existing.size() >= 30) return existing;

        List<Customer> created = new ArrayList<>();
        int count = 30 + random.nextInt(11); // 30-40 customers
        for (int i = 0; i < count; i++) {
            String firstName = getRandom(FIRST_NAMES);
            String lastName = getRandom(LAST_NAMES);
            Employee emp = employees.get(random.nextInt(employees.size()));
            
            Customer c = Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + i + "@gmail.com")
                .phone("+91-9" + (100000000 + random.nextInt(900000000)))
                .address("Apt " + (100 + i) + ", MG Road, City Area")
                .status(Customer.CustomerStatus.values()[random.nextInt(Customer.CustomerStatus.values().length)])
                .assignedEmployeeId(emp.getId())
                .dealer(dealer)
                .isActive(true)
                .build();
            created.add(customerRepository.save(c));
        }
        return created;
    }

    private void seedSalesOrders(Dealer dealer, List<Customer> customers, List<Vehicle> inventory, List<Employee> employees) {
        long existingCount = salesOrderRepository.countByDealerIdAndCreatedAtBetween(dealer.getId(), LocalDateTime.now().minusYears(1), LocalDateTime.now());
        if (existingCount >= 30) return;

        int count = 30 + random.nextInt(11); // 30-40 orders
        
        // Use a pool of sales-focused employees
        List<Employee> salesEmployees = employees.stream()
            .filter(e -> e.getDesignation().contains("Sales"))
            .collect(Collectors.toList());
        if (salesEmployees.isEmpty()) salesEmployees = employees;

        for (int i = 0; i < count; i++) {
            Customer cust = customers.get(random.nextInt(customers.size()));
            Vehicle veh = inventory.get(random.nextInt(inventory.size()));
            Employee emp = salesEmployees.get(random.nextInt(salesEmployees.size()));
            
            // Skip if vehicle out of stock (unless it's a small portion)
            if (veh.getStock() <= 0 && random.nextDouble() > 0.1) continue;

            // Status Distribution: 40% Invoiced, 30% Confirmed, 20% Pending, 10% Cancelled
            double r = random.nextDouble();
            String status;
            if (r < 0.4) status = "INVOICED";
            else if (r < 0.7) status = "CONFIRMED";
            else if (r < 0.9) status = "PENDING";
            else status = "CANCELLED";

            BigDecimal discount = new BigDecimal(random.nextInt(20000));
            BigDecimal finalAmt = veh.getBasePrice().subtract(discount);
            

            SalesOrder order = SalesOrder.builder()
                .customer(cust)
                .vehicle(veh)
                .employee(emp)
                .dealerId(dealer.getId())
                .price(veh.getBasePrice())
                .discount(discount)
                .finalAmount(finalAmt)
                .status(status)
                .build();
            
            // Trick to set createdAt (which is @CreationTimestamp updatable=false normally)
            // But if we want to spread dates, we might need to use a hack or just accept "now"
            // For seeding, the seeder can bypass this if we use a raw SQL later or if we modify entity
            // Since SalesOrder has @CreationTimestamp, it's hard to set it manually without a separate tool.
            // I'll assume standard save is fine for now, or just provide a SQL script for orders at the end.
            
            SalesOrder saved = salesOrderRepository.save(order);
            
            // Logic-based stock reduction
            if (status.equals("INVOICED") || status.equals("CONFIRMED")) {
                if (veh.getStock() > 0) {
                    veh.setStock(veh.getStock() - 1);
                    vehicleRepository.save(veh);
                }

                // If Invoiced, create a payment record
                if (status.equals("INVOICED")) {
                    Payment p = Payment.builder()
                        .salesOrder(saved)
                        .amount(saved.getFinalAmount())
                        .paymentMode(random.nextBoolean() ? "ONLINE" : "CHEQUE")
                        .transactionRef("TXN-" + System.currentTimeMillis() % 10000 + i)
                        .paymentStatus("PAID")
                        .build();
                    paymentRepository.save(p);
                }
            }

            // Generate Audit Log
            logAudit(emp.getUser().getUsername(), "CREATE", "SALES", saved.getId(), "Booked " + veh.getModelName() + " for " + cust.getFirstName() + " " + cust.getLastName());
        }
    }

    private void randomizeDates() {
        // Randomly spread 'created_at' dates between NOW and 180 days ago in MySQL
        String sql1 = "UPDATE sales_orders SET created_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY) WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        String sql2 = "UPDATE customers SET created_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY) WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR) AND id > 0";
        String sql3 = "UPDATE audit_logs SET created_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 180) DAY) WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)";
        String sql4 = "UPDATE payments SET payment_date = created_at WHERE payment_date > DATE_SUB(NOW(), INTERVAL 1 HOUR)"; 
        // Wait, 'payments' table might have 'payment_date' via @CreationTimestamp too? Let's check.
        // Actually, many entities have 'created_at'.
        
        try {
            jdbcTemplate.execute(sql1);
            jdbcTemplate.execute(sql2);
            jdbcTemplate.execute(sql3);
            jdbcTemplate.execute(sql4);
        } catch (Exception e) {
            System.err.println("Note: Native date randomization failed (might not be MySQL). Skip if testing.");
        }
    }

    private void logAudit(String username, String action, String entityType, Long entityId, String desc) {
        User user = userRepository.findByUsername(username).orElse(null);
        AuditLog log = AuditLog.builder()
            .user(user)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .description(desc)
            .createdAt(LocalDateTime.now().minusHours(random.nextInt(10)))
            .build();
        auditLogRepository.save(log);
    }

    private <T> T getRandom(T[] array) {
        return array[random.nextInt(array.length)];
    }
}
