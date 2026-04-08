package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.CustomerDto;
import com.hyundai.dms.entity.Customer;
import com.hyundai.dms.entity.Dealer;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.CustomerRepository;
import com.hyundai.dms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final com.hyundai.dms.repository.DealerRepository dealerRepository;
    private final com.hyundai.dms.service.AuditService auditService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Long getCurrentDealerId() {
        User user = getCurrentUser();
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
    public Page<CustomerDto> getAllCustomers(String search, String statusStr, Long assignedEmployeeId, Pageable pageable) {
        Customer.CustomerStatus status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try { status = Customer.CustomerStatus.valueOf(statusStr.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        if (isAdmin()) {
            return customerRepository.findAllActiveWithSearch(search, status, pageable).map(this::mapToDto);
        }
        Long dealerId = getCurrentDealerId();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isEmployee = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EMPLOYEE"));
        if (isEmployee && !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEALER"))) {
            User currentUser = getCurrentUser();
            return customerRepository.findActiveByAssignedEmployee(dealerId, currentUser.getId(), search, status, pageable).map(this::mapToDto);
        }
        return customerRepository.findActiveWithSearch(dealerId, search, status, assignedEmployeeId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!isAdmin()) {
            Long dealerId = getCurrentDealerId();
            Dealer d = customer.getDealer();
            if (d == null || !dealerId.equals(d.getId())) {
                throw new ResourceNotFoundException("Customer not found in your dealership");
            }
        }
        return mapToDto(customer);
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        Long dealerId = getCurrentDealerId();

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealership not found"));

        Customer.CustomerStatus status = Customer.CustomerStatus.NEW;
        if (customerDto.getStatus() != null && !customerDto.getStatus().isEmpty()) {
            try { status = Customer.CustomerStatus.valueOf(customerDto.getStatus()); }
            catch (IllegalArgumentException ignored) {}
        }

        Customer c = Customer.builder()
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .email(customerDto.getEmail())
                .phone(customerDto.getPhone())
                .address(customerDto.getAddress())
                .notes(customerDto.getNotes())
                .status(status)
                .assignedEmployeeId(customerDto.getAssignedEmployeeId())
                .nextFollowUpDate(customerDto.getNextFollowUpDate())
                .isActive(true)
                .dealer(dealer)
                .build();
        Customer saved = customerRepository.save(c);
        auditService.logAction("CREATE", "CUSTOMER", saved.getId(), "Created customer: " + saved.getFirstName() + " " + saved.getLastName());
        return mapToDto(saved);
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto dto) {
        Long dealerId = getCurrentDealerId();
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!isAdmin() && (c.getDealer() == null || !c.getDealer().getId().equals(dealerId))) {
            throw new ResourceNotFoundException("Customer not found in your dealership");
        }

        c.setFirstName(dto.getFirstName());
        c.setLastName(dto.getLastName());
        c.setEmail(dto.getEmail());
        c.setPhone(dto.getPhone());
        c.setAddress(dto.getAddress());
        c.setNotes(dto.getNotes());
        c.setAssignedEmployeeId(dto.getAssignedEmployeeId());
        c.setNextFollowUpDate(dto.getNextFollowUpDate());
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            try { c.setStatus(Customer.CustomerStatus.valueOf(dto.getStatus())); }
            catch (IllegalArgumentException ignored) {}
        }
        
        Customer saved = customerRepository.save(c);
        auditService.logAction("UPDATE", "CUSTOMER", saved.getId(), "Updated customer: " + saved.getFirstName() + " " + saved.getLastName());
        return mapToDto(saved);
    }

    @Transactional
    public void archiveCustomer(Long id) {
        Long dealerId = getCurrentDealerId();
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!isAdmin() && (c.getDealer() == null || !c.getDealer().getId().equals(dealerId))) {
            throw new ResourceNotFoundException("Customer not found in your dealership");
        }

        c.setIsActive(false);
        customerRepository.save(c);
    }

    private CustomerDto mapToDto(Customer c) {
        String empName = null;
        if (c.getAssignedEmployeeId() != null) {
            empName = userRepository.findById(c.getAssignedEmployeeId())
                    .map(User::getFullName).orElse(null);
        }
        Long dealerId = null;
        Dealer d = c.getDealer();
        if (d != null) {
            dealerId = d.getId();
        }
        
        return CustomerDto.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .address(c.getAddress())
                .notes(c.getNotes())
                .status(c.getStatus() != null ? c.getStatus().name() : "NEW")
                .assignedEmployeeId(c.getAssignedEmployeeId())
                .assignedEmployeeName(empName)
                .nextFollowUpDate(c.getNextFollowUpDate())
                .isActive(c.getIsActive())
                .dealerId(dealerId)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
