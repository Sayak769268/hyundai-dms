package com.hyundai.dms.controller;

import com.hyundai.dms.dto.UserDto;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.exception.ResourceNotFoundException;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    /** Employees list for "Assign To" dropdown, scoped to dealer */
    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<List<Map<String, Object>>> getEmployees() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<User> userList;
        if (isAdmin) {
            userList = userRepository.findByRoleNameAndDealerId("ROLE_EMPLOYEE", currentUser.getDealerId()); // Even for admin, usually want one dealer context or all
        } else {
            userList = userRepository.findByRoleNameAndDealerId("ROLE_EMPLOYEE", currentUser.getDealerId());
        }

        List<Map<String, Object>> employees = userList.stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getFullName() != null ? u.getFullName() : u.getUsername()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(employees);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String role,
            Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return ResponseEntity.ok(userService.getAllUsers(isAdmin ? null : currentUser.getDealerId(), role, search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return ResponseEntity.ok(userService.getUserById(id, isAdmin ? null : currentUser.getDealerId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return new ResponseEntity<>(userService.createUser(userDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        return ResponseEntity.ok(userService.updateUser(id, userDto, isAdmin ? null : currentUser.getDealerId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        userService.deleteUser(id, isAdmin ? null : currentUser.getDealerId());
        return ResponseEntity.noContent().build();
    }
}
