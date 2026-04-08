package com.hyundai.dms.controller;

import com.hyundai.dms.dto.DashboardStatsDto;
import com.hyundai.dms.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<DashboardStatsDto> getStats(@org.springframework.web.bind.annotation.RequestParam(required = false) Long dealerId) {
        return ResponseEntity.ok(dashboardService.getDashboardStats(dealerId));
    }
}
