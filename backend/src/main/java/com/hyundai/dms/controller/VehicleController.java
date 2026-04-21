package com.hyundai.dms.controller;

import com.hyundai.dms.dto.VehicleDto;
import com.hyundai.dms.service.impl.VehicleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleServiceImpl vehicleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<VehicleDto>> getAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) Integer year,
            Pageable pageable) {
        log.info("GET /api/vehicles — search='{}', status={}, year={}", search, status, year);
        return ResponseEntity.ok(vehicleService.getAllVehicles(search, status, minPrice, maxPrice, year, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<VehicleDto> getById(@PathVariable Long id) {
        log.info("GET /api/vehicles/{}", id);
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<VehicleDto> create(@RequestBody VehicleDto dto) {
        log.info("POST /api/vehicles — model='{}', variant='{}'", dto.getModelName(), dto.getVariant());
        return new ResponseEntity<>(vehicleService.createVehicle(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<VehicleDto> update(@PathVariable Long id, @RequestBody VehicleDto dto) {
        log.info("PUT /api/vehicles/{}", id);
        return ResponseEntity.ok(vehicleService.updateVehicle(id, dto));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<VehicleDto> adjustStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        int delta = body.getOrDefault("delta", 0);
        log.info("PATCH /api/vehicles/{}/stock — delta={}", id, delta);
        return ResponseEntity.ok(vehicleService.adjustStock(id, delta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/vehicles/{}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/models")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<java.util.List<String>> getDistinctModels() {
        return ResponseEntity.ok(vehicleService.getDistinctModels());
    }

    @GetMapping("/variants")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<java.util.List<String>> getVariantsByModel(@RequestParam String modelName) {
        return ResponseEntity.ok(vehicleService.getVariantsByModel(modelName));
    }
}
