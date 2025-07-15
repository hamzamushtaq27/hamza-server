package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 기본 조회 메서드
    Optional<User> findByEmail(String email);
    
    Optional<User> findByNickname(String nickname);
    
    boolean existsByEmail(String email);
    
    boolean existsByNickname(String nickname);
    
    // 활성 사용자 조회
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    // 이메일 인증 관련
    Optional<User> findByEmailAndEmailVerifiedTrue(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    // 제공자별 조회
    List<User> findByProvider(AuthProvider provider);
    
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    // 관리자 조회
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findActiveAdmins();
    
    // 통계 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countUsersByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :startDate")
    Long countUsersByLastLoginAfter(@Param("startDate") LocalDateTime startDate);
    
    // 최근 로그인 사용자 조회
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :startDate ORDER BY u.lastLogin DESC")
    List<User> findRecentActiveUsers(@Param("startDate") LocalDateTime startDate);
    
    // 비활성 사용자 조회
    @Query("SELECT u FROM User u WHERE u.isActive = false OR u.lastLogin < :cutoffDate")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 이메일 미인증 사용자 조회
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    List<User> findUnverifiedUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 사용자 상태 업데이트
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.email = :email")
    void verifyEmail(@Param("email") String email);
    
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, @Param("isActive") boolean isActive);
    
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, @Param("password") String password);
    
    // 검색 기능
    @Query("SELECT u FROM User u WHERE " +
           "(u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%) " +
           "AND u.isActive = true " +
           "ORDER BY u.createdAt DESC")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    // 닉네임 중복 체크 (현재 사용자 제외)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.nickname = :nickname AND u.id != :userId")
    boolean existsByNicknameAndNotUserId(@Param("nickname") String nickname, @Param("userId") Long userId);
    
    // 사용자 프로필 업데이트를 위한 조회
    @Query("SELECT u FROM User u WHERE u.id = :userId AND u.isActive = true")
    Optional<User> findByIdAndIsActiveTrue(@Param("userId") Long userId);
    
    // 월별 가입 통계
    @Query("SELECT FUNCTION('YEAR', u.createdAt) as year, " +
           "FUNCTION('MONTH', u.createdAt) as month, " +
           "COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY FUNCTION('YEAR', u.createdAt), FUNCTION('MONTH', u.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getUserRegistrationStatistics(@Param("startDate") LocalDateTime startDate);
    
    // 제공자별 통계
    @Query("SELECT u.provider, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.provider")
    List<Object[]> getUserProviderStatistics();
}