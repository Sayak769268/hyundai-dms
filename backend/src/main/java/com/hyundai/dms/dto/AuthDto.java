package com.hyundai.dms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

public class AuthDto {
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        private String username;
        
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Full Name is required")
        private String fullName;

        @NotBlank(message = "Password is required")
        private String password;

        private String role; // "ROLE_ADMIN", "ROLE_DEALER", "ROLE_EMPLOYEE"

        private Long dealerId; // Must be set for non-admin users
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private List<String> roles;
        private List<String> permissions;
        private Long dealerId;
        private String dealerName;
    }
}
