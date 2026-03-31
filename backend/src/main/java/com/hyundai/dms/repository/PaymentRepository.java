package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySalesOrderId(Long salesOrderId);
}
