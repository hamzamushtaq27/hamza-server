package com.dgsw.hamza.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal, accessTokenExpiration, "access");
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal, refreshTokenExpiration, "refresh");
    }

    /**
     * 사용자 정보로 Access Token 생성
     */
    public String generateAccessToken(Long userId, String email, String role) {
        return generateToken(userId, email, role, accessTokenExpiration, "access");
    }

    /**
     * 사용자 정보로 Refresh Token 생성
     */
    public String generateRefreshToken(Long userId, String email, String role) {
        return generateToken(userId, email, role, refreshTokenExpiration, "refresh");
    }

    /**
     * 공통 토큰 생성 메서드
     */
    private String generateToken(UserPrincipal userPrincipal, long expiration, String tokenType) {
        return generateToken(userPrincipal.getId(), userPrincipal.getEmail(), 
                           userPrincipal.getRole(), expiration, tokenType);
    }

    /**
     * 공통 토큰 생성 메서드
     */
    private String generateToken(Long userId, String email, String role, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("role", role);
        claims.put("tokenType", tokenType);
        claims.put("iat", now.getTime() / 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 토큰에서 이메일 추출
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰에서 역할 추출
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰에서 만료 시간 추출
     */
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * 토큰에서 발급 시간 추출
     */
    public Date getIssuedAtFromToken(String token) {
        return parseClaims(token).getIssuedAt();
    }

    /**
     * 토큰 타입 확인
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT token validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Access Token 여부 확인
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh Token 여부 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰 남은 시간 계산 (밀리초)
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationFromToken(token);
            return expiration.getTime() - new Date().getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 토큰이 곧 만료되는지 확인 (5분 이내)
     */
    public boolean isTokenExpiringSoon(String token) {
        long remainingTime = getTokenRemainingTime(token);
        return remainingTime > 0 && remainingTime < 300000; // 5분 = 300,000ms
    }

    /**
     * 토큰에서 모든 클레임 추출
     */
    public Map<String, Object> getAllClaimsFromToken(String token) {
        return parseClaims(token);
    }

    /**
     * 토큰 파싱 및 클레임 추출
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰 헤더에서 Bearer 제거
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰 정보 로깅을 위한 메서드
     */
    public void logTokenInfo(String token) {
        try {
            Claims claims = parseClaims(token);
            log.info("Token Info - Subject: {}, Issued At: {}, Expiration: {}, Type: {}", 
                    claims.getSubject(), 
                    claims.getIssuedAt(), 
                    claims.getExpiration(),
                    claims.get("tokenType"));
        } catch (Exception e) {
            log.error("Error logging token info: {}", e.getMessage());
        }
    }
}