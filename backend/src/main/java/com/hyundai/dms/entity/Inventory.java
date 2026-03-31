package com.hyundai.dms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private VehicleVariant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", nullable = false)
    private Dealer dealer;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "engine_number", unique = true, length = 50)
    private String engineNumber;

    @Column(length = 50)
    private String color;

    @Column(length = 50, columnDefinition = "varchar(50) default 'AVAILABLE'")
    private String status;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;
}
