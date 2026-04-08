package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderDto {
    private Long id;
    private Long customerId;
    private Long vehicleId;
    private Long employeeId;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String status;
    private LocalDateTime createdAt;

    // Additional data for frontend table/listing convenience
    private String customerName;
    private String vehicleName;
    private String vehicleVariant;
    private String employeeName;
    private Long dealerId;
    private String dealerName;
}
