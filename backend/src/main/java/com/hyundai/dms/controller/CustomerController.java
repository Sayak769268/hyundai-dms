package com.hyundai.dms.controller;

import com.hyundai.dms.dto.CustomerDto;
import com.hyundai.dms.service.impl.CustomerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerServiceImpl customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<CustomerDto>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long assignedEmployeeId,
            Pageable pageable) {
        log.info("GET /api/customers — search='{}', status={}, assignedEmployee={}", search, status, assignedEmployeeId);
        return ResponseEntity.ok(customerService.getAllCustomers(search, status, assignedEmployeeId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<CustomerDto> getOne(@PathVariable Long id) {
        log.info("GET /api/customers/{}", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<CustomerDto> create(@RequestBody CustomerDto dto) {
        log.info("POST /api/customers — name='{} {}'", dto.getFirstName(), dto.getLastName());
        return new ResponseEntity<>(customerService.createCustomer(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<CustomerDto> update(@PathVariable Long id, @RequestBody CustomerDto dto) {
        log.info("PUT /api/customers/{}", id);
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<Void> archive(@PathVariable Long id) {
        log.info("PATCH /api/customers/{}/archive", id);
        customerService.archiveCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
