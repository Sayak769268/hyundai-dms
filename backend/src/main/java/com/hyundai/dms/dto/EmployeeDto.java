package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDto {
    private Long id;
    private Long userId;
    private Long departmentId;
    private String employeeCode;
    private String designation;
    private LocalDate hireDate;
    private String fullName;
}
