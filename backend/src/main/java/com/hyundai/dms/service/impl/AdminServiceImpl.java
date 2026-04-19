package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.AdminDashboardDto;
import com.hyundai.dms.dto.DealerRankDto;
import com.hyundai.dms.entity.Dealer;
import com.hyundai.dms.entity.Vehicle;
import com.hyundai.dms.repository.DealerRepository;
import com.hyundai.dms.repository.SalesOrderRepository;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl {
    private final DealerRepository dealerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboard() {
        LocalDateTime firstOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long totalDealers = dealerRepository.count();
        long totalUsers = userRepository.count();
        long globalSales = salesOrderRepository.countByCreatedAtBetween(firstOfThisMonth, now);
        BigDecimal globalRevenue = salesOrderRepository.sumTotalRevenue(firstOfThisMonth, now);
        if (globalRevenue == null) globalRevenue = BigDecimal.ZERO;

        List<Dealer> dealers = dealerRepository.findAll();
        List<DealerRankDto> dealerRankings = new ArrayList<>();

        for (Dealer dealer : dealers) {
            long dealerSalesAllTime = salesOrderRepository.countByDealerIdAndStatusNot(dealer.getId(), "CANCELLED");
            BigDecimal dealerRev = salesOrderRepository.sumRevenueByDealerId(dealer.getId());
            if (dealerRev == null) dealerRev = BigDecimal.ZERO;

            dealerRankings.add(DealerRankDto.builder()
                    .dealerId(dealer.getId())
                    .dealerName(dealer.getName())
                    .location(dealer.getAddress() != null ? dealer.getAddress() : "Unknown Location")
                    .isActive(dealer.getIsActive() == null || dealer.getIsActive())
                    .totalSales(dealerSalesAllTime)
                    .totalRevenue(dealerRev)
                    .build());
        }

        // Sort by revenue descending
        dealerRankings.sort((d1, d2) -> d2.getTotalRevenue().compareTo(d1.getTotalRevenue()));

        DealerRankDto topDealer = dealerRankings.isEmpty() ? null : dealerRankings.get(0);
        DealerRankDto worstDealer = dealerRankings.isEmpty() ? null : dealerRankings.get(dealerRankings.size() - 1);

        // Group low stock alerts by dealer — one summary per dealer
        List<String> alerts = new ArrayList<>();
        List<Vehicle> lowStockVehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getStock() != null && v.getStock() < 3)
                .collect(Collectors.toList());

        // Group by dealerId
        java.util.Map<Long, List<Vehicle>> byDealer = lowStockVehicles.stream()
                .filter(v -> v.getDealerId() != null)
                .collect(Collectors.groupingBy(Vehicle::getDealerId));

        for (java.util.Map.Entry<Long, List<Vehicle>> entry : byDealer.entrySet()) {
            String dName = dealerRepository.findById(entry.getKey()).map(Dealer::getName).orElse("Unknown Dealer");
            List<Vehicle> vehicles = entry.getValue();
            String vehicleList = vehicles.stream()
                    .map(v -> v.getModelName() + " (" + v.getStock() + " left)")
                    .collect(Collectors.joining(", "));
            alerts.add(dName + " — " + vehicles.size() + " units low stock|" + vehicleList);
        }

        return AdminDashboardDto.builder()
                .totalDealers(totalDealers)
                .totalUsers(totalUsers)
                .globalSalesThisMonth(globalSales)
                .globalRevenueThisMonth(globalRevenue)
                .dealerRankings(dealerRankings)
                .topDealer(topDealer)
                .worstDealer(worstDealer)
                .globalAlerts(alerts)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<DealerRankDto> searchDealers(
            String name, Boolean status, Long minSales, Long maxSales, 
            java.math.BigDecimal minRevenue, java.math.BigDecimal maxRevenue, 
            Pageable pageable) {
        
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter("search_debug.log", true))) {
            out.println("--- SEARCH REQUEST @ " + java.time.LocalDateTime.now() + " ---");
            out.println("name=" + name + ", status=" + status + ", minS=" + minSales + ", maxS=" + maxSales + 
                        ", minR=" + minRevenue + ", maxR=" + maxRevenue);
            out.flush();
        } catch (java.io.IOException e) {}
        
        return dealerRepository.searchDealers(
                name, status, minSales, maxSales, minRevenue, maxRevenue, pageable);
    }

    @Transactional
    public void toggleDealerStatus(Long dealerId) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new RuntimeException("Dealer not found"));
        boolean newStatus = !(dealer.getIsActive() != null && dealer.getIsActive());
        dealer.setIsActive(newStatus);
        dealerRepository.save(dealer);

        // Also lock/unlock all users belonging to this dealer
        userRepository.findAllByDealerId(dealerId).forEach(user -> {
            user.setIsActive(newStatus);
            userRepository.save(user);
        });
    }
}
