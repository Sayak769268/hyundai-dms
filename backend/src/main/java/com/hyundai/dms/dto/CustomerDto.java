package com.hyundai.dms.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String notes;
    private String status;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private LocalDate nextFollowUpDate;
    private Boolean isActive;
    private Long dealerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
