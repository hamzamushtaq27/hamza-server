package com.dgsw.hamza.security;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TokenInfo {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private String scope;
    private Long userId;
    private String userEmail;
    private String userRole;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isRefreshExpired() {
        return LocalDateTime.now().isAfter(refreshExpiresAt);
    }
    
    public long getExpiresIn() {
        if (expiresAt == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toSeconds();
    }
}