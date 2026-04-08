package com.hyundai.dms.service.impl;

import com.hyundai.dms.dto.VehicleDto;
import com.hyundai.dms.entity.User;
import com.hyundai.dms.entity.Vehicle;
import com.hyundai.dms.exception.ResourceNotFoundException;
import com.hyundai.dms.repository.UserRepository;
import com.hyundai.dms.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final com.hyundai.dms.service.AuditService auditService;

    private Long getCurrentDealerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getDealerId() == null) {
            throw new AccessDeniedException("User is not associated with any dealership");
        }
        return user.getDealerId();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional(readOnly = true)
    public java.util.List<String> getDistinctModels() {
        if (isAdmin()) return vehicleRepository.findDistinctModelNamesByDealerId(null);
        Long dealerId = getCurrentDealerId();
        return vehicleRepository.findDistinctModelNamesByDealerId(dealerId);
    }

    @Transactional(readOnly = true)
    public java.util.List<String> getVariantsByModel(String modelName) {
        if (isAdmin()) return vehicleRepository.findDistinctVariantsByDealerIdAndModelName(null, modelName);
        Long dealerId = getCurrentDealerId();
        return vehicleRepository.findDistinctVariantsByDealerIdAndModelName(dealerId, modelName);
    }

    @Transactional(readOnly = true)
    public Page<VehicleDto> getAllVehicles(String search, String status, java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Integer year, Pageable pageable) {
        if (isAdmin()) {
            return vehicleRepository.findWithSearchAll(search, status, minPrice, maxPrice, year, pageable).map(this::mapToDto);
        }
        Long dealerId = getCurrentDealerId();
        return vehicleRepository.findWithSearch(dealerId, search, status, minPrice, maxPrice, year, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public VehicleDto getVehicleById(Long id) {
        if (isAdmin()) {
            Vehicle v = vehicleRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
            return mapToDto(v);
        }
        Long dealerId = getCurrentDealerId();
        Vehicle v = vehicleRepository.findById(id)
                .filter(veh -> {
                    Long vDealerId = veh.getDealerId();
                    return vDealerId != null && vDealerId.equals(dealerId);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in your inventory"));
        return mapToDto(v);
    }

    @Transactional
    public VehicleDto createVehicle(VehicleDto dto) {
        Long dealerId = getCurrentDealerId();
        Vehicle v = Vehicle.builder()
                .modelName(dto.getModelName())
                .brand(dto.getBrand() != null ? dto.getBrand() : "Hyundai")
                .variant(dto.getVariant())
                .year(dto.getYear())
                .basePrice(dto.getBasePrice())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .dealerId(dealerId)
                .build();
        Vehicle saved = vehicleRepository.save(v);
        auditService.logAction("CREATE", "INVENTORY", saved.getId(), "Added vehicle: " + saved.getModelName() + " (" + saved.getVariant() + ")");
        return mapToDto(saved);
    }

    @Transactional
    public VehicleDto updateVehicle(Long id, VehicleDto dto) {
        Long dealerId = getCurrentDealerId();
        Vehicle v = vehicleRepository.findById(id)
                .filter(veh -> {
                    Long vDealerId = veh.getDealerId();
                    return isAdmin() || (vDealerId != null && vDealerId.equals(dealerId));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in your inventory"));
        v.setModelName(dto.getModelName());
        v.setBrand(dto.getBrand() != null ? dto.getBrand() : "Hyundai");
        v.setVariant(dto.getVariant());
        v.setYear(dto.getYear());
        v.setBasePrice(dto.getBasePrice());
        Vehicle saved = vehicleRepository.save(v);
        auditService.logAction("UPDATE", "INVENTORY", saved.getId(), "Updated vehicle: " + saved.getModelName());
        return mapToDto(saved);
    }

    @Transactional
    public VehicleDto adjustStock(Long id, int delta) {
        Long dealerId = getCurrentDealerId();
        Vehicle v = vehicleRepository.findById(id)
                .filter(veh -> {
                    Long vDealerId = veh.getDealerId();
                    return isAdmin() || (vDealerId != null && vDealerId.equals(dealerId));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in your inventory"));
        int current = v.getStock() != null ? v.getStock() : 0;
        int newStock = current + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot go below 0. Current: " + current + ", adjustment: " + delta);
        }
        v.setStock(newStock);
        Vehicle saved = vehicleRepository.save(v);
        auditService.logAction("UPDATE", "STOCK", saved.getId(), (delta > 0 ? "Increased" : "Decreased") + " stock of " + saved.getModelName() + " by " + Math.abs(delta) + ". New stock: " + saved.getStock());
        return mapToDto(saved);
    }

    @Transactional
    public void deleteVehicle(Long id) {
        Long dealerId = getCurrentDealerId();
        Vehicle v = vehicleRepository.findById(id)
                .filter(veh -> {
                    Long vDealerId = veh.getDealerId();
                    return isAdmin() || (vDealerId != null && vDealerId.equals(dealerId));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found in your inventory"));
        vehicleRepository.delete(v);
    }

    private VehicleDto mapToDto(Vehicle v) {
        return VehicleDto.builder()
                .id(v.getId())
                .modelName(v.getModelName())
                .brand(v.getBrand())
                .variant(v.getVariant())
                .year(v.getYear())
                .basePrice(v.getBasePrice())
                .stock(v.getStock())
                .stockStatus(v.getStockStatus())
                .dealerId(v.getDealerId())
                .updatedAt(v.getUpdatedAt())
                .build();
    }
}
