package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.UserDto;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.AuthProvider;
import com.dgsw.hamza.enums.UserRole;
import com.dgsw.hamza.repository.UserRepository;
import com.dgsw.hamza.security.JwtTokenProvider;
import com.dgsw.hamza.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto.UserInfo signUp(UserDto.SignUpRequest request) {
        log.info("Attempting to sign up user with email: {}", request.getEmail());
        
        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }
        
        // 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new RuntimeException("이미 사용 중인 닉네임입니다.");
        }
        
        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .provider(AuthProvider.LOCAL)
                .role(UserRole.USER)
                .isActive(true)
                .emailVerified(true) // 개발 환경에서는 자동으로 인증처리
                .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("User signed up successfully with email: {}", savedUser.getEmail());
        
        return convertToUserInfo(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public UserDto.TokenResponse signIn(UserDto.SignInRequest request) {
        log.info("Attempting to sign in user with email: {}", request.getEmail());
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // 마지막 로그인 시간 업데이트
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userRepository.updateLastLogin(userPrincipal.getId(), LocalDateTime.now());
        
        // 사용자 정보 조회
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        log.info("User signed in successfully with email: {}", request.getEmail());
        
        return UserDto.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenRemainingTime(accessToken))
                .userInfo(convertToUserInfo(user))
                .build();
    }

    /**
     * 토큰 갱신
     */
    @Transactional(readOnly = true)
    public UserDto.TokenResponse refreshToken(UserDto.RefreshTokenRequest request) {
        log.info("Attempting to refresh token");
        
        String refreshToken = request.getRefreshToken();
        
        // 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }
        
        // 리프레시 토큰인지 확인
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 아닙니다.");
        }
        
        // 사용자 정보 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        if (!user.isActive()) {
            throw new RuntimeException("비활성화된 사용자입니다.");
        }
        
        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email, role);
        
        log.info("Token refreshed successfully for user: {}", email);
        
        return UserDto.TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getTokenRemainingTime(newAccessToken))
                .userInfo(convertToUserInfo(user))
                .build();
    }

    /**
     * 이메일 중복 확인
     */
    @Transactional(readOnly = true)
    public UserDto.EmailCheckResponse checkEmailAvailability(String email) {
        log.info("Checking email availability: {}", email);
        
        boolean available = !userRepository.existsByEmail(email);
        String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
        
        return UserDto.EmailCheckResponse.builder()
                .available(available)
                .message(message)
                .build();
    }

    /**
     * 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    public UserDto.EmailCheckResponse checkNicknameAvailability(String nickname) {
        log.info("Checking nickname availability: {}", nickname);
        
        boolean available = !userRepository.existsByNickname(nickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        
        return UserDto.EmailCheckResponse.builder()
                .available(available)
                .message(message)
                .build();
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, UserDto.PasswordChangeRequest request) {
        log.info("Attempting to change password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new RuntimeException("새 비밀번호가 일치하지 않습니다.");
        }
        
        // 비밀번호 업데이트
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * 프로필 업데이트
     */
    @Transactional
    public UserDto.UserInfo updateProfile(Long userId, UserDto.ProfileUpdateRequest request) {
        log.info("Attempting to update profile for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 닉네임 중복 확인 (현재 사용자 제외)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNicknameAndNotUserId(request.getNickname(), userId)) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
            user.setNickname(request.getNickname());
        }
        
        // 다른 필드 업데이트
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        
        User savedUser = userRepository.save(user);
        
        log.info("Profile updated successfully for user: {}", userId);
        
        return convertToUserInfo(savedUser);
    }

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserDto.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return convertToUserInfo(user);
    }

    /**
     * User 엔티티를 UserInfo DTO로 변환
     */
    private UserDto.UserInfo convertToUserInfo(User user) {
        return UserDto.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .phoneNumber(user.getPhoneNumber())
                .birthDate(user.getBirthDate())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .emailVerified(user.isEmailVerified())
                .isActive(user.isActive())
                .build();
    }
}