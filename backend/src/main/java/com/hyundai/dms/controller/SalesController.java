package com.hyundai.dms.controller;

import com.hyundai.dms.dto.SalesOrderDto;
import com.hyundai.dms.service.impl.SalesServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesServiceImpl salesService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<SalesOrderDto>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minAmount,
            @RequestParam(required = false) java.math.BigDecimal maxAmount,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Pageable pageable) {
        java.time.LocalDateTime from = fromDate != null && !fromDate.isEmpty() ? java.time.LocalDate.parse(fromDate).atStartOfDay() : null;
        java.time.LocalDateTime to = toDate != null && !toDate.isEmpty() ? java.time.LocalDate.parse(toDate).atTime(23, 59, 59) : null;
        return ResponseEntity.ok(salesService.getAllSalesOrders(search, status, minAmount, maxAmount, from, to, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<SalesOrderDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(salesService.getSalesOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<SalesOrderDto> create(@RequestBody SalesOrderDto dto) {
        return new ResponseEntity<>(salesService.createSalesOrder(dto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<SalesOrderDto> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.getOrDefault("status", "PENDING").toUpperCase();
        return ResponseEntity.ok(salesService.updateOrderStatus(id, status));
    }
}
