package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByUserUsername(String username);
    Page<Employee> findAllByDealerId(Long dealerId, Pageable pageable);
    Optional<Employee> findByIdAndDealerId(Long id, Long dealerId);
}
