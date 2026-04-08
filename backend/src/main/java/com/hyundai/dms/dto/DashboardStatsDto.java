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
public class DashboardStatsDto {
    private long totalCustomers;
    private long vehiclesInStock;
    private long salesThisMonth;
    private BigDecimal revenueThisMonth;

    private String salesGrowthLabel; // e.g. "+5 from last month"
    private String revenueGrowthLabel; // e.g. "+10%"
    private boolean salesGrowthPositive;
    private boolean revenueGrowthPositive;

    private long lowStockCount;
    private long pendingOrdersCount;
    private long pendingFollowUpsCount;

    private String topSellingModel;
    private long topModelSales;

    private long todayOrders;
    private BigDecimal todayRevenue;

    private List<SalesOrderDto> recentOrders;
    private List<ChartData> chartData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartData {
        private String label;
        private double value;
    }
}
