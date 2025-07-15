package com.dgsw.hamza.util;

import com.dgsw.hamza.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * 현재 인증된 사용자 정보 가져오기
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) principal);
        }
        
        return Optional.empty();
    }

    /**
     * 현재 사용자 ID 가져오기
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }

    /**
     * 현재 사용자 이메일 가져오기
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(UserPrincipal::getEmail);
    }

    /**
     * 현재 사용자 역할 가져오기
     */
    public static Optional<String> getCurrentUserRole() {
        return getCurrentUser().map(UserPrincipal::getRole);
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    public static boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(UserPrincipal::isAdmin)
                .orElse(false);
    }

    /**
     * 현재 사용자가 상담사인지 확인
     */
    public static boolean isCurrentUserTherapist() {
        return getCurrentUser()
                .map(UserPrincipal::isTherapist)
                .orElse(false);
    }

    /**
     * 현재 사용자가 일반 사용자인지 확인
     */
    public static boolean isCurrentUserUser() {
        return getCurrentUser()
                .map(UserPrincipal::isUser)
                .orElse(false);
    }

    /**
     * 현재 사용자가 특정 사용자인지 확인
     */
    public static boolean isCurrentUser(Long userId) {
        return getCurrentUserId()
                .map(id -> id.equals(userId))
                .orElse(false);
    }

    /**
     * 현재 사용자가 본인의 리소스에 접근하는지 또는 관리자인지 확인
     */
    public static boolean canAccessUserResource(Long userId) {
        return isCurrentUser(userId) || isCurrentUserAdmin();
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String);
    }

    /**
     * 현재 사용자 닉네임 가져오기
     */
    public static Optional<String> getCurrentUserNickname() {
        return getCurrentUser().map(UserPrincipal::getNickname);
    }

    /**
     * 현재 사용자가 활성 상태인지 확인
     */
    public static boolean isCurrentUserActive() {
        return getCurrentUser()
                .map(user -> user.isEnabled())
                .orElse(false);
    }
}