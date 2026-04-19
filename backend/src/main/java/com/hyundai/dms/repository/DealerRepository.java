package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.Optional;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    Optional<Dealer> findByName(String name);
    
    @org.springframework.data.jpa.repository.Query(
        "SELECT new com.hyundai.dms.dto.DealerRankDto(d.id, d.name, d.address, d.isActive, " +
        "  (SELECT COUNT(s) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED'), " +
        "  (SELECT COALESCE(SUM(s.finalAmount), 0.0) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED')) " +
        "FROM Dealer d " +
        "WHERE (:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
        "AND (:status IS NULL OR d.isActive = :status) " +
        "AND (:minSales IS NULL OR (SELECT COUNT(s) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED') >= :minSales) " +
        "AND (:maxSales IS NULL OR (SELECT COUNT(s) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED') <= :maxSales) " +
        "AND (:minRevenue IS NULL OR (SELECT COALESCE(SUM(s.finalAmount), 0.0) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED') >= :minRevenue) " +
        "AND (:maxRevenue IS NULL OR (SELECT COALESCE(SUM(s.finalAmount), 0.0) FROM SalesOrder s WHERE s.dealerId = d.id AND s.status != 'CANCELLED') <= :maxRevenue)"
    )
    org.springframework.data.domain.Page<com.hyundai.dms.dto.DealerRankDto> searchDealers(
            @org.springframework.data.repository.query.Param("name") String name, 
            @org.springframework.data.repository.query.Param("status") Boolean status, 
            @org.springframework.data.repository.query.Param("minSales") Long minSales, 
            @org.springframework.data.repository.query.Param("maxSales") Long maxSales, 
            @org.springframework.data.repository.query.Param("minRevenue") java.math.BigDecimal minRevenue, 
            @org.springframework.data.repository.query.Param("maxRevenue") java.math.BigDecimal maxRevenue, 
            org.springframework.data.domain.Pageable pageable);
}
