package com.dgsw.hamza.controller;

import com.dgsw.hamza.security.UserPrincipal;
import com.dgsw.hamza.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Test", description = "테스트용 API")
@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "공개 API 테스트", description = "인증 없이 접근 가능한 API")
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "공개 API 테스트 성공");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", SecurityUtils.isAuthenticated());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "인증 API 테스트", description = "인증이 필요한 API")
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authTest(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "인증 API 테스트 성공");
        response.put("timestamp", System.currentTimeMillis());
        response.put("authenticated", true);
        
        if (userPrincipal != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userPrincipal.getId());
            userInfo.put("email", userPrincipal.getEmail());
            userInfo.put("nickname", userPrincipal.getNickname());
            userInfo.put("role", userPrincipal.getRole());
            
            response.put("user", userInfo);
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "관리자 API 테스트", description = "관리자만 접근 가능한 API")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> adminTest(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "관리자 API 테스트 성공");
        response.put("timestamp", System.currentTimeMillis());
        response.put("admin", userPrincipal.isAdmin());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 권한 테스트", description = "사용자 역할별 권한 테스트")
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> rolesTest(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "사용자 권한 테스트");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> permissions = new HashMap<>();
        permissions.put("isAdmin", userPrincipal.isAdmin());
        permissions.put("isTherapist", userPrincipal.isTherapist());
        permissions.put("isUser", userPrincipal.isUser());
        
        response.put("permissions", permissions);
        response.put("authorities", userPrincipal.getAuthorities());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Security Utils 테스트", description = "Security 유틸리티 메서드 테스트")
    @GetMapping("/security-utils")
    public ResponseEntity<Map<String, Object>> securityUtilsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Security Utils 테스트");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> securityInfo = new HashMap<>();
        securityInfo.put("isAuthenticated", SecurityUtils.isAuthenticated());
        securityInfo.put("currentUserId", SecurityUtils.getCurrentUserId().orElse(null));
        securityInfo.put("currentUserEmail", SecurityUtils.getCurrentUserEmail().orElse(null));
        securityInfo.put("currentUserRole", SecurityUtils.getCurrentUserRole().orElse(null));
        securityInfo.put("isCurrentUserAdmin", SecurityUtils.isCurrentUserAdmin());
        securityInfo.put("isCurrentUserTherapist", SecurityUtils.isCurrentUserTherapist());
        securityInfo.put("isCurrentUserUser", SecurityUtils.isCurrentUserUser());
        securityInfo.put("isCurrentUserActive", SecurityUtils.isCurrentUserActive());
        
        response.put("securityInfo", securityInfo);
        
        return ResponseEntity.ok(response);
    }
}