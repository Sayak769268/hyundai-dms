package com.hyundai.dms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "exchanges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private CustomerLead lead;

    @Column(name = "old_vehicle_make", length = 100)
    private String oldVehicleMake;

    @Column(name = "old_vehicle_model", length = 100)
    private String oldVehicleModel;

    private Integer year;

    private Integer mileage;

    @Column(name = "offered_value", precision = 15, scale = 2)
    private BigDecimal offeredValue;

    @Column(length = 50, columnDefinition = "varchar(50) default 'INSPECTING'")
    private String status;
}
