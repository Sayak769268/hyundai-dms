package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    long countByIsActiveTrueAndDealerId(Long dealerId);

    // Active customers only, with optional search across name, email, phone (Dealer-scoped)
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.dealer.id = :dealerId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:assignedEmployeeId IS NULL OR c.assignedEmployeeId = :assignedEmployeeId) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR c.phone LIKE CONCAT('%',:search,'%'))")
    Page<Customer> findActiveWithSearch(
            @Param("dealerId") Long dealerId,
            @Param("search") String search,
            @Param("status") Customer.CustomerStatus status,
            @Param("assignedEmployeeId") Long assignedEmployeeId,
            Pageable pageable);

    // Admin-scoped: see all active customers across all dealers
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR c.phone LIKE CONCAT('%',:search,'%'))")
    Page<Customer> findAllActiveWithSearch(
            @Param("search") String search, 
            @Param("status") Customer.CustomerStatus status,
            Pageable pageable);

    // For employee: only see their assigned customers and their specific dealer
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.dealer.id = :dealerId AND c.assignedEmployeeId = :empId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(c.firstName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Customer> findActiveByAssignedEmployee(
            @Param("dealerId") Long dealerId, 
            @Param("empId") Long empId, 
            @Param("search") String search, 
            @Param("status") Customer.CustomerStatus status,
            Pageable pageable);

    Optional<Customer> findByIdAndDealerId(Long id, Long dealerId);
}
