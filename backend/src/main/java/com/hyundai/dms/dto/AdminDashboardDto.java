package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    private long totalDealers;
    private long totalUsers;
    private long globalSalesThisMonth;
    private BigDecimal globalRevenueThisMonth;
    private List<DealerRankDto> dealerRankings;
    private DealerRankDto topDealer;
    private DealerRankDto worstDealer;
    private List<String> globalAlerts;
}
