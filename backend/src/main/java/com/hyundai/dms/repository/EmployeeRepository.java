package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByUserUsername(String username);
    Page<Employee> findAllByDealerId(Long dealerId, Pageable pageable);
    Optional<Employee> findByIdAndDealerId(Long id, Long dealerId);
    Optional<Employee> findFirstByDealerId(Long dealerId);

    @Query("SELECT e FROM Employee e WHERE e.dealerId = :dealerId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.user.fullName) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(e.user.email) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(e.designation) LIKE LOWER(CONCAT('%',:search,'%'))) AND " +
           "(:designation IS NULL OR :designation = '' OR LOWER(e.designation) LIKE LOWER(CONCAT('%',:designation,'%')))")
    Page<Employee> findWithSearch(
            @Param("dealerId") Long dealerId,
            @Param("search") String search,
            @Param("designation") String designation,
            Pageable pageable);
}
