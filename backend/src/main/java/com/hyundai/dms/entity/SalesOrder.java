package com.hyundai.dms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Optional linking to employee handling the sale
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "dealer_id")
    private Long dealerId;

    // Legacy columns from old schema — kept nullable with defaults to avoid constraint errors
    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "booking_amount", precision = 15, scale = 2)
    private BigDecimal bookingAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(precision = 15, scale = 2)
    private BigDecimal discount;

    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @Column(nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (discount == null) discount = BigDecimal.ZERO;
        if (status == null) status = "PENDING";
        // Set legacy columns to safe defaults so old NOT NULL constraints don't fail
        if (totalAmount == null) totalAmount = finalAmount != null ? finalAmount : BigDecimal.ZERO;
        if (bookingAmount == null) bookingAmount = BigDecimal.ZERO;
    }}
