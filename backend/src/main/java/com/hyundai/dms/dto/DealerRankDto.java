package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DealerRankDto {
    private Long dealerId;
    private String dealerName;
    private String location;
    private boolean isActive;
    private long totalSales;
    private BigDecimal totalRevenue;
}
