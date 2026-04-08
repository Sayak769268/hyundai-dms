package com.hyundai.dms.repository;

import com.hyundai.dms.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    // Find if same customer has booked same vehicle and order is active (not cancelled)
    boolean existsByCustomerIdAndVehicleIdAndStatusNot(Long customerId, Long vehicleId, String status);

    // List for Employee role to see ONLY their sales
    Page<SalesOrder> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<SalesOrder> findAllByDealerId(Long dealerId, Pageable pageable);

    @Query("SELECT s FROM SalesOrder s WHERE s.dealerId = :dealerId AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:minAmount IS NULL OR s.finalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR s.finalAmount <= :maxAmount) AND " +
           "(:fromDate IS NULL OR s.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR s.createdAt <= :toDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.customer.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.customer.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.vehicle.modelName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.vehicle.variant) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<SalesOrder> findWithFilters(
            @Param("dealerId") Long dealerId,
            @Param("search") String search,
            @Param("status") String status,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            Pageable pageable);

    @Query("SELECT s FROM SalesOrder s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:minAmount IS NULL OR s.finalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR s.finalAmount <= :maxAmount) AND " +
           "(:fromDate IS NULL OR s.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR s.createdAt <= :toDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(s.customer.firstName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.customer.lastName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.vehicle.modelName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(s.vehicle.variant) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<SalesOrder> findWithFiltersAdmin(
            @Param("search") String search,
            @Param("status") String status,
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            Pageable pageable);

    // Global
    List<SalesOrder> findTop10ByOrderByCreatedAtDesc();

    // Recent orders across all
    List<SalesOrder> findTop10ByDealerIdOrderByCreatedAtDesc(Long dealerId);

    // Stats queries
    long countByDealerIdAndCreatedAtBetween(Long dealerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.finalAmount) FROM SalesOrder s WHERE s.dealerId = :dealerId AND s.createdAt BETWEEN :start AND :end AND s.status <> 'CANCELLED'")
    BigDecimal sumRevenueByDealerIdAndCreatedAtBetween(@Param("dealerId") Long dealerId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByDealerIdAndStatus(Long dealerId, String status);

    @Query("SELECT s.vehicle.modelName, COUNT(s) as cnt FROM SalesOrder s WHERE s.dealerId = :dealerId GROUP BY s.vehicle.modelName ORDER BY cnt DESC")
    List<Object[]> findTopSellingModelsByDealerId(@Param("dealerId") Long dealerId);

    // Global Stats for OEM Admin
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.finalAmount) FROM SalesOrder s WHERE s.createdAt BETWEEN :start AND :end AND s.status <> 'CANCELLED'")
    BigDecimal sumTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s.vehicle.modelName, COUNT(s) as cnt FROM SalesOrder s GROUP BY s.vehicle.modelName ORDER BY cnt DESC")
    List<Object[]> findTopSellingModelsGlobal();

    long countByStatus(String status);
}
