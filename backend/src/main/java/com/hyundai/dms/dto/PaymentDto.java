package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    private Long id;
    private Long salesOrderId;
    private BigDecimal amount;
    private String paymentMode;
    private String transactionRef;
    private String paymentStatus;
    private LocalDateTime paymentDate;
}
