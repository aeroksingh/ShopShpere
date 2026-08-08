package com.shopsphere.shopsphere.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String tokenType; // "Bearer"
    private Long userId;
    private String fullName;
    private String email;
    private String role;
}
