package com.hyundai.dms.repository;

import com.hyundai.dms.entity.TestDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {
    Page<TestDrive> findAllByDealerId(Long dealerId, Pageable pageable);
}
