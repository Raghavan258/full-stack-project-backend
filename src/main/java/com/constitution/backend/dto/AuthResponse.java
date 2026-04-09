package com.constitution.backend.dto;

import com.constitution.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private User.Role role;
    private boolean verified;
}
