package com.hyundai.dms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(length = 100)
    private String brand;

    @Column(length = 100)
    private String variant;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "dealer_id")
    private Long dealerId;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Auto-calculated from stock value.
     * 0 → OUT_OF_STOCK, 1-2 → LOW_STOCK, 3+ → AVAILABLE
     */
    public String getStockStatus() {
        if (stock == null || stock == 0) return "OUT_OF_STOCK";
        if (stock < 3) return "LOW_STOCK";
        return "AVAILABLE";
    }

    @PrePersist
    public void prePersist() {
        if (stock == null) stock = 0;
        if (brand == null) brand = "Hyundai";
    }
}
