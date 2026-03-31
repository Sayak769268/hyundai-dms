package com.hyundai.dms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Boolean isActive;
    private Set<String> roles;
    private Long dealerId;
    private String dealerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
