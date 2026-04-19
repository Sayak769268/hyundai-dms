package com.hyundai.dms.controller;

import com.hyundai.dms.dto.TestDriveDto;
import com.hyundai.dms.entity.Customer;
import com.hyundai.dms.entity.TestDrive;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.entity.Vehicle;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.CustomerRepository;
import com.hyundai.dms.repository.TestDriveRepository;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveRepository testDriveRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    private Long getCurrentDealerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getDealerId() == null) throw new AccessDeniedException("No dealership associated");
        return user.getDealerId();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<Page<TestDriveDto>> getAll(Pageable pageable) {
        Long dealerId = getCurrentDealerId();
        return ResponseEntity.ok(
            testDriveRepository.findAllByDealerId(dealerId, pageable)
                .map(this::toDto)
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TestDriveDto> create(@RequestBody TestDriveDto dto) {
        Long dealerId = getCurrentDealerId();

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Only NEW or INTERESTED customers
        String status = customer.getStatus() != null ? customer.getStatus().name() : "";
        if (!status.equals("NEW") && !status.equals("INTERESTED")) {
            throw new IllegalArgumentException("Test drives can only be scheduled for New or Interested customers.");
        }

        TestDrive td = TestDrive.builder()
                .customer(customer)
                .vehicle(vehicle)
                .dealerId(dealerId)
                .scheduledDate(dto.getScheduledDate())
                .notes(dto.getNotes())
                .status("SCHEDULED")
                .build();

        TestDrive saved = testDriveRepository.save(td);
        return new ResponseEntity<>(toDto(saved), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_DEALER', 'ROLE_EMPLOYEE')")
    public ResponseEntity<TestDriveDto> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        TestDrive td = testDriveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test drive not found"));
        td.setStatus(body.getOrDefault("status", td.getStatus()));
        return ResponseEntity.ok(toDto(testDriveRepository.save(td)));
    }

    private TestDriveDto toDto(TestDrive td) {
        return TestDriveDto.builder()
                .id(td.getId())
                .customerId(td.getCustomer().getId())
                .customerName(td.getCustomer().getFirstName() + " " + td.getCustomer().getLastName())
                .customerPhone(td.getCustomer().getPhone())
                .vehicleId(td.getVehicle().getId())
                .vehicleName(td.getVehicle().getModelName() + (td.getVehicle().getVariant() != null ? " " + td.getVehicle().getVariant() : ""))
                .dealerId(td.getDealerId())
                .scheduledDate(td.getScheduledDate())
                .status(td.getStatus())
                .notes(td.getNotes())
                .createdAt(td.getCreatedAt())
                .build();
    }
}
