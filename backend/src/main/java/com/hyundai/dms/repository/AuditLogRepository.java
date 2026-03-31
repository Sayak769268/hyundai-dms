package com.hyundai.dms.repository;

import com.hyundai.dms.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a LEFT JOIN a.user u WHERE " +
           "(:dealerId IS NULL OR u.dealerId = :dealerId) AND " +
           "(:actionType IS NULL OR a.action = :actionType) AND " +
           "(:keyword IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AuditLog> findWithFilters(
            @Param("dealerId") Long dealerId,
            @Param("actionType") String actionType,
            @Param("keyword") String keyword,
            Pageable pageable);
}
