package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.SalesOrderDto;
import com.hyundai.dms.entity.Customer;
import com.hyundai.dms.entity.Employee;
import com.hyundai.dms.entity.SalesOrder;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.entity.Vehicle;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.CustomerRepository;
import com.hyundai.dms.repository.DealerRepository;
import com.hyundai.dms.repository.EmployeeRepository;
import com.hyundai.dms.repository.SalesOrderRepository;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesServiceImpl {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    private final DealerRepository dealerRepository;
    private final com.hyundai.dms.service.AuditService auditService;

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

    @Transactional(readOnly = true)
    public Page<SalesOrderDto> getAllSalesOrders(String search, String status, java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate, Pageable pageable) {
        String s  = (search == null || search.isBlank()) ? null : search;
        String st = (status == null || status.isBlank()) ? null : status;

        if (isAdmin()) {
            return salesOrderRepository.findWithFiltersAdmin(s, st, minAmount, maxAmount, fromDate, toDate, pageable).map(this::mapToDto);
        }

        Long dealerId = getCurrentDealerId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployee) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            Employee employee = currentUser != null ? employeeRepository.findByUserId(currentUser.getId()).orElse(null) : null;
            if (employee != null) {
                return salesOrderRepository.findByEmployeeId(employee.getId(), pageable).map(this::mapToDto);
            }
            return Page.empty(pageable);
        }

        return salesOrderRepository.findWithFilters(dealerId, s, st, minAmount, maxAmount, fromDate, toDate, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public SalesOrderDto getSalesOrderById(Long id) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));

        if (!isAdmin()) {
            Long dealerId = getCurrentDealerId();
            if (!dealerId.equals(order.getDealerId())) {
                throw new ResourceNotFoundException("Sales Order not found in your dealership");
            }
        }
        return mapToDto(order);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public SalesOrderDto createSalesOrder(SalesOrderDto dto) {
        Long dealerId = getCurrentDealerId();

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Ensure customer and vehicle belong to this dealer
        if (!isAdmin()) {
            if (customer.getDealer() == null || !customer.getDealer().getId().equals(dealerId)) {
                throw new AccessDeniedException("Customer does not belong to your dealership");
            }
            if (vehicle.getDealerId() == null || !vehicle.getDealerId().equals(dealerId)) {
                throw new AccessDeniedException("Vehicle does not belong to your dealership");
            }
        }

        if (vehicle.getStock() <= 0) {
            throw new IllegalArgumentException("Selected vehicle is out of stock");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        Employee employee = currentUser != null ? employeeRepository.findByUserId(currentUser.getId()).orElse(null) : null;

        // If dealer user (no employee record), find any active employee in their dealership
        if (employee == null) {
            employee = employeeRepository.findFirstByDealerId(dealerId).orElse(null);
        }

        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        BigDecimal finalAmount = vehicle.getBasePrice().subtract(discount);

        SalesOrder order = SalesOrder.builder()
                .customer(customer)
                .vehicle(vehicle)
                .employee(employee)
                .dealerId(dealerId)
                .price(vehicle.getBasePrice())
                .discount(discount)
                .finalAmount(finalAmount)
                .status("PENDING")
                .build();

        vehicle.setStock(vehicle.getStock() - 1);
        vehicleRepository.save(vehicle);
        
        SalesOrder savedOrder = salesOrderRepository.save(order);
        if (savedOrder == null) throw new RuntimeException("Failed to save order");
        
        auditService.logAction(
            "CREATE", 
            "SALES", 
            savedOrder.getId(), 
            "Created a new sales order for customer: " + customer.getFirstName() + " " + customer.getLastName() + " (Vehicle: " + vehicle.getModelName() + ")"
        );

        return mapToDto(savedOrder);
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public SalesOrderDto updateOrderStatus(Long id, String status) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found"));

        if (!isAdmin()) {
            Long dealerId = getCurrentDealerId();
            if (!dealerId.equals(order.getDealerId())) {
                throw new ResourceNotFoundException("Sales Order not found in your dealership");
            }
        }

        order.setStatus(status);
        SalesOrder updatedOrder = salesOrderRepository.save(order);
        
        auditService.logAction(
            "UPDATE", 
            "SALES", 
            updatedOrder.getId(), 
            "Updated sales order status to: " + status
        );
        
        return mapToDto(updatedOrder);
    }

    private SalesOrderDto mapToDto(SalesOrder s) {
        String dealerName = "Unknown";
        if (s.getDealerId() != null) {
            Long dealerId = s.getDealerId();
            if (dealerId != null) {
                dealerName = dealerRepository.findById(dealerId)
                        .map(d -> d.getName())
                        .orElse("Unknown");
            }
        }

        return SalesOrderDto.builder()
                .id(s.getId())
                .customerId(s.getCustomer().getId())
                .customerName(s.getCustomer().getFirstName() + " " + s.getCustomer().getLastName())
                .vehicleId(s.getVehicle().getId())
                .vehicleName(s.getVehicle().getModelName() + " - " + s.getVehicle().getVariant())
                .employeeName(s.getEmployee() != null ? s.getEmployee().getUser().getFullName() : "N/A")
                .dealerId(s.getDealerId())
                .dealerName(dealerName)
                .price(s.getPrice())
                .discount(s.getDiscount())
                .finalAmount(s.getFinalAmount())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
