package com.hyundai.dms.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto {
    private Long id;
    private String modelName;
    private String brand;
    private String variant;
    private Integer year;
    private BigDecimal basePrice;
    private Integer stock;
    private String stockStatus;
    private Long dealerId;
    private LocalDateTime updatedAt;
}
