package com.hyundai.dms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;
}
