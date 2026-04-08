package com.hyundai.dms.repository;

import com.hyundai.dms.entity.CustomerLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerLeadRepository extends JpaRepository<CustomerLead, Long> {
}
