package com.hyundai.dms.controller;

import com.hyundai.dms.dto.EmployeeDto;
import com.hyundai.dms.entity.Employee;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.EmployeeRepository;
import com.hyundai.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    private Long getCurrentDealerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getDealerId() == null) {
            throw new AccessDeniedException("User is not associated with any dealership");
        }
        return user.getDealerId();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<Page<EmployeeDto>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String designation,
            Pageable pageable) {
        if (isAdmin()) {
            return ResponseEntity.ok(employeeRepository.findAll(pageable).map(this::mapToDto));
        }
        Long dealerId = getCurrentDealerId();
        String s = search.isEmpty() ? null : search;
        String d = designation.isEmpty() ? null : designation;
        return ResponseEntity.ok(employeeRepository.findWithSearch(dealerId, s, d, pageable).map(this::mapToDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<EmployeeDto> getById(@PathVariable Long id) {
        if (isAdmin()) {
            Employee e = employeeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
            return ResponseEntity.ok(mapToDto(e));
        }
        Long dealerId = getCurrentDealerId();
        Employee e = employeeRepository.findByIdAndDealerId(id, dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found in your dealership"));
        return ResponseEntity.ok(mapToDto(e));
    }

    private EmployeeDto mapToDto(Employee e) {
        return EmployeeDto.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .fullName(e.getUser().getFullName())
                .employeeCode(e.getEmployeeCode())
                .designation(e.getDesignation())
                .hireDate(e.getHireDate())
                .build();
    }
}
