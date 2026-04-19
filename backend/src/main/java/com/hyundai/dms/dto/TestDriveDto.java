package com.hyundai.dms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestDriveDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long vehicleId;
    private String vehicleName;
    private Long dealerId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
