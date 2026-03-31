package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.DashboardStatsDto;
import com.hyundai.dms.dto.SalesOrderDto;
import com.hyundai.dms.entity.SalesOrder;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.CustomerRepository;
import com.hyundai.dms.repository.SalesOrderRepository;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl {

    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(Long targetDealerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long dealerId = currentUser.getDealerId();
        
        // If Admin requests a specific dealer, override the dealerId
        if (isAdmin && targetDealerId != null) {
            dealerId = targetDealerId;
            isAdmin = false; // Treat them as a dealer for the duration of this method to get dealer-specific stats
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime firstOfLastMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long salesThisMonth;
        BigDecimal revenueThisMonth;
        long salesLastMonth;
        BigDecimal revenueLastMonth;
        long lowStockCount;
        long pendingOrdersCount;
        List<Object[]> topSellingData;
        long todayOrders;
        BigDecimal todayRevenue;
        List<SalesOrderDto> recent;
        List<DashboardStatsDto.ChartData> chartData = new ArrayList<>();
        Long totalInStock;
        long totalCustomers;

        if (isAdmin) {
            salesThisMonth = salesOrderRepository.countByCreatedAtBetween(firstOfThisMonth, now);
            revenueThisMonth = salesOrderRepository.sumTotalRevenue(firstOfThisMonth, now);
            salesLastMonth = salesOrderRepository.countByCreatedAtBetween(firstOfLastMonth, firstOfThisMonth);
            revenueLastMonth = salesOrderRepository.sumTotalRevenue(firstOfLastMonth, firstOfThisMonth);
            lowStockCount = vehicleRepository.countByStockLessThan(3);
            pendingOrdersCount = salesOrderRepository.countByStatus("PENDING");
            topSellingData = salesOrderRepository.findTopSellingModelsGlobal();
            todayOrders = salesOrderRepository.countByCreatedAtBetween(todayStart, now);
            todayRevenue = salesOrderRepository.sumTotalRevenue(todayStart, now);
            recent = salesOrderRepository.findTop10ByOrderByCreatedAtDesc()
                    .stream().map(this::mapToDto).collect(Collectors.toList());
            totalInStock = vehicleRepository.sumTotalStockGlobal();
            totalCustomers = customerRepository.count(); // Global count
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = LocalDate.now().minusMonths(i);
                LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
                LocalDateTime end = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
                long count = salesOrderRepository.countByCreatedAtBetween(start, end);
                chartData.add(new DashboardStatsDto.ChartData(monthDate.getMonth().name().substring(0, 3), (double) count));
            }
        } else {
            if (dealerId == null) {
                throw new org.springframework.security.access.AccessDeniedException("User is not associated with any dealership");
            }
            salesThisMonth = salesOrderRepository.countByDealerIdAndCreatedAtBetween(dealerId, firstOfThisMonth, now);
            revenueThisMonth = salesOrderRepository.sumRevenueByDealerIdAndCreatedAtBetween(dealerId, firstOfThisMonth, now);
            salesLastMonth = salesOrderRepository.countByDealerIdAndCreatedAtBetween(dealerId, firstOfLastMonth, firstOfThisMonth);
            revenueLastMonth = salesOrderRepository.sumRevenueByDealerIdAndCreatedAtBetween(dealerId, firstOfLastMonth, firstOfThisMonth);
            lowStockCount = vehicleRepository.countByStockLessThanAndDealerId(3, dealerId);
            pendingOrdersCount = salesOrderRepository.countByDealerIdAndStatus(dealerId, "PENDING");
            topSellingData = salesOrderRepository.findTopSellingModelsByDealerId(dealerId);
            todayOrders = salesOrderRepository.countByDealerIdAndCreatedAtBetween(dealerId, todayStart, now);
            todayRevenue = salesOrderRepository.sumRevenueByDealerIdAndCreatedAtBetween(dealerId, todayStart, now);
            recent = salesOrderRepository.findTop10ByDealerIdOrderByCreatedAtDesc(dealerId)
                    .stream().map(this::mapToDto).collect(Collectors.toList());
            totalInStock = vehicleRepository.sumTotalStockByDealerId(dealerId);
            totalCustomers = customerRepository.countByIsActiveTrueAndDealerId(dealerId);
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthDate = LocalDate.now().minusMonths(i);
                LocalDateTime start = monthDate.withDayOfMonth(1).atStartOfDay();
                LocalDateTime end = monthDate.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
                long count = salesOrderRepository.countByDealerIdAndCreatedAtBetween(dealerId, start, end);
                chartData.add(new DashboardStatsDto.ChartData(monthDate.getMonth().name().substring(0, 3), (double) count));
            }
        }

        if (revenueThisMonth == null) revenueThisMonth = BigDecimal.ZERO;
        if (revenueLastMonth == null) revenueLastMonth = BigDecimal.ZERO;
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        // Growth Calculation
        long salesDiff = salesThisMonth - salesLastMonth;
        String salesGrowthLabel = (salesDiff >= 0 ? "+" : "") + salesDiff + " from last month";
        
        String revenueGrowthLabel = "+0%";
        boolean revPositive = true;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = revenueThisMonth.subtract(revenueLastMonth);
            BigDecimal growthPercent = diff.divide(revenueLastMonth, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            revenueGrowthLabel = (growthPercent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + growthPercent.setScale(1, RoundingMode.HALF_UP) + "%";
            revPositive = growthPercent.compareTo(BigDecimal.ZERO) >= 0;
        }

        String topModel = "N/A";
        long topSales = 0;
        if (!topSellingData.isEmpty()) {
            topModel = (String) topSellingData.get(0)[0];
            topSales = (long) topSellingData.get(0)[1];
        }

        return DashboardStatsDto.builder()
                .totalCustomers(totalCustomers)
                .vehiclesInStock(totalInStock != null ? totalInStock : 0)
                .salesThisMonth(salesThisMonth)
                .revenueThisMonth(revenueThisMonth)
                .salesGrowthLabel(salesGrowthLabel)
                .revenueGrowthLabel(revenueGrowthLabel)
                .salesGrowthPositive(salesDiff >= 0)
                .revenueGrowthPositive(revPositive)
                .lowStockCount(lowStockCount)
                .pendingOrdersCount(pendingOrdersCount)
                .pendingFollowUpsCount(0)
                .topSellingModel(topModel)
                .topModelSales(topSales)
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .recentOrders(recent)
                .chartData(chartData)
                .build();
    }

    private SalesOrderDto mapToDto(SalesOrder s) {
        return SalesOrderDto.builder()
                .id(s.getId())
                .customerName(s.getCustomer().getFirstName() + " " + s.getCustomer().getLastName())
                .vehicleName(s.getVehicle().getModelName())
                .finalAmount(s.getFinalAmount())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
