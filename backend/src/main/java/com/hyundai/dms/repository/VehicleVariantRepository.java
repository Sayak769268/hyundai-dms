package com.hyundai.dms.repository;

import com.hyundai.dms.entity.VehicleVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VehicleVariantRepository extends JpaRepository<VehicleVariant, Long> {
    List<VehicleVariant> findByModelId(Long modelId);
}
