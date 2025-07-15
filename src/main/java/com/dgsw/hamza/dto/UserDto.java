package com.dgsw.hamza.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpRequest {
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수 입력 항목입니다.")
        private String passwordConfirm;

        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
        private String nickname;

        @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
        @Pattern(regexp = "^(010|011|016|017|018|019)-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식을 입력해주세요.")
        private String phoneNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignInRequest {
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        private String email;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImage;
        private String phoneNumber;
        private LocalDateTime birthDate;
        private String role;
        private LocalDateTime createdAt;
        private LocalDateTime lastLogin;
        private Boolean emailVerified;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        @Builder.Default
        private String tokenType = "Bearer";
        private Long expiresIn;
        private UserInfo userInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailCheckRequest {
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식을 입력해주세요.")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailCheckResponse {
        private boolean available;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PasswordChangeRequest {
        @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
                message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인은 필수 입력 항목입니다.")
        private String newPasswordConfirm;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProfileUpdateRequest {
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.")
        private String nickname;

        private String profileImage;
        private String phoneNumber;
        private LocalDateTime birthDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefreshTokenRequest {
        @NotBlank(message = "리프레시 토큰은 필수 입력 항목입니다.")
        private String refreshToken;
    }
}