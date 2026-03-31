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

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> authenticateUser(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            List<String> authorities = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<String> roles = authorities.stream()
                    .filter(a -> a.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            List<String> permissions = authorities.stream()
                    .filter(a -> !a.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            // Get dealerId and dealerName for the response
            com.hyundai.dms.entity.User dbUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            Long dealerId = dbUser != null ? dbUser.getDealerId() : null;
            String dealerName = null;
            if (dealerId != null) {
                dealerName = dealerRepository.findById(dealerId)
                        .map(com.hyundai.dms.entity.Dealer::getName).orElse(null);
            }

            // Audit successful login
            if (dbUser != null) {
                try {
                    auditService.logAction("LOGIN", "SYSTEM", dbUser.getId(), "User logged in: " + dbUser.getUsername());
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok(AuthDto.AuthResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .roles(roles)
                    .permissions(permissions)
                    .dealerId(dealerId)
                    .dealerName(dealerName)
                    .build());
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Log failed attempt if username is valid but password was wrong
            userRepository.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                 try {
                    auditService.logAction("FAILED_LOGIN", "SYSTEM", user.getId(), "Failed login attempt for user: " + user.getUsername());
                } catch (Exception ignored) {}
            });
            throw e; // Handled by GlobalExceptionHandler
        }
    }
}
