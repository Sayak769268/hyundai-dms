package com.hyundai.dms.controller;

import com.hyundai.dms.dto.AdminDashboardDto;
import com.hyundai.dms.service.impl.AdminServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminServiceImpl adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboard());
    }

    @GetMapping("/dealers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<com.hyundai.dms.dto.DealerRankDto>> searchDealers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Boolean status,
            @RequestParam(value = "minSales", required = false) Long minSales,
            @RequestParam(value = "maxSales", required = false) Long maxSales,
            @RequestParam(value = "minRevenue", required = false) java.math.BigDecimal minRevenue,
            @RequestParam(value = "maxRevenue", required = false) java.math.BigDecimal maxRevenue,
            org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(adminService.searchDealers(name, status, minSales, maxSales, minRevenue, maxRevenue, pageable));
    }

    @PostMapping("/dealer/{id}/toggle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> toggleDealer(@PathVariable Long id) {
        adminService.toggleDealerStatus(id);
        return ResponseEntity.ok().build();
    }
}
