package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.UserDto;
import com.dgsw.hamza.security.UserPrincipal;
import com.dgsw.hamza.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Authentication", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일 또는 닉네임")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDto.SignUpRequest request) {
        log.info("Sign up request received for email: {}", request.getEmail());
        
        try {
            UserDto.UserInfo userInfo = authService.signUp(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "회원가입이 완료되었습니다.");
            response.put("user", userInfo);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Sign up failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "회원가입 실패");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody UserDto.SignInRequest request) {
        log.info("Sign in request received for email: {}", request.getEmail());
        
        try {
            UserDto.TokenResponse tokenResponse = authService.signIn(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "로그인이 완료되었습니다.");
            response.put("data", tokenResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Sign in failed for email: {}, error: {}", request.getEmail(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "로그인 실패");
            errorResponse.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody UserDto.RefreshTokenRequest request) {
        log.info("Token refresh request received");
        
        try {
            UserDto.TokenResponse tokenResponse = authService.refreshToken(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "토큰이 갱신되었습니다.");
            response.put("data", tokenResponse);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "토큰 갱신 실패");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "이메일 중복 확인 완료")
    })
    @GetMapping("/check-email")
    public ResponseEntity<UserDto.EmailCheckResponse> checkEmailAvailability(
            @Parameter(description = "확인할 이메일") @RequestParam String email) {
        log.info("Email availability check request for: {}", email);
        
        UserDto.EmailCheckResponse response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 중복 확인 완료")
    })
    @GetMapping("/check-nickname")
    public ResponseEntity<UserDto.EmailCheckResponse> checkNicknameAvailability(
            @Parameter(description = "확인할 닉네임") @RequestParam String nickname) {
        log.info("Nickname availability check request for: {}", nickname);
        
        UserDto.EmailCheckResponse response = authService.checkNicknameAvailability(nickname);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "인증된 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto.UserInfo> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Current user info request for user: {}", userPrincipal.getId());
        
        UserDto.UserInfo userInfo = authService.getCurrentUser(userPrincipal.getId());
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserDto.PasswordChangeRequest request) {
        log.info("Password change request for user: {}", userPrincipal.getId());
        
        try {
            authService.changePassword(userPrincipal.getId(), request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Password change failed for user: {}, error: {}", userPrincipal.getId(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "비밀번호 변경 실패");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "프로필 업데이트", description = "사용자의 프로필 정보를 업데이트합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserDto.ProfileUpdateRequest request) {
        log.info("Profile update request for user: {}", userPrincipal.getId());
        
        try {
            UserDto.UserInfo userInfo = authService.updateProfile(userPrincipal.getId(), request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "프로필이 성공적으로 업데이트되었습니다.");
            response.put("user", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Profile update failed for user: {}, error: {}", userPrincipal.getId(), e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "프로필 업데이트 실패");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        log.info("Logout request for user: {}", authentication.getName());
        
        // 현재는 클라이언트에서 토큰을 제거하도록 응답만 반환
        // 향후 토큰 블랙리스트 기능을 추가할 수 있음
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "로그아웃이 완료되었습니다.");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계정 비활성화", description = "사용자 계정을 비활성화합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "계정 비활성화 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @DeleteMapping("/deactivate")
    public ResponseEntity<?> deactivateAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Account deactivation request for user: {}", userPrincipal.getId());
        
        // 실제 계정 비활성화 로직은 추후 구현
        Map<String, Object> response = new HashMap<>();
        response.put("message", "계정 비활성화 요청이 접수되었습니다.");
        
        return ResponseEntity.ok(response);
    }
}