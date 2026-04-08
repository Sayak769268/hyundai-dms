package com.hyundai.dms.controller;

import com.hyundai.dms.service.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final DataSeederService dataSeederService;

    @PostMapping("/run")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> runSeeder() {
        try {
            dataSeederService.seedAll();
            return ResponseEntity.ok("Data seeding completed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error seeding data: " + e.getMessage());
        }
    }
}
