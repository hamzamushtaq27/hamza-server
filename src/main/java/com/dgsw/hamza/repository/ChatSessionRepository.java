package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.ChatSession;
import com.dgsw.hamza.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * 사용자의 활성 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user AND cs.isActive = true ORDER BY cs.endedAt DESC")
    List<ChatSession> findActiveSessionsByUser(@Param("user") User user);

    /**
     * 사용자의 가장 최근 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user ORDER BY cs.endedAt DESC LIMIT 1")
    Optional<ChatSession> findLatestSessionByUser(@Param("user") User user);

    /**
     * 세션 ID로 활성 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.sessionId = :sessionId AND cs.isActive = true")
    Optional<ChatSession> findActiveSessionBySessionId(@Param("sessionId") String sessionId);

    /**
     * 사용자의 세션 히스토리 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user ORDER BY cs.createdAt DESC")
    List<ChatSession> findSessionHistoryByUser(@Param("user") User user);

    /**
     * 특정 기간 내 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user " +
           "AND cs.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cs.createdAt DESC")
    List<ChatSession> findSessionsByUserAndDateRange(@Param("user") User user,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 비활성 세션 조회 (특정 시간 이후 활동이 없는 세션)
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.isActive = true AND cs.endedAt < :cutoffTime")
    List<ChatSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 활성 세션 수 조회
     */
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.isActive = true")
    Long countActiveSessions();

    /**
     * 사용자의 총 세션 수 조회
     */
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.user = :user")
    Long countSessionsByUser(@Param("user") User user);

    /**
     * 오늘 생성된 세션 수 조회
     */
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE DATE(cs.createdAt) = DATE(NOW())")
    Long countTodaySessions();

    /**
     * 특정 기간 내 생성된 세션 수 조회
     */
    @Query("SELECT COUNT(cs) FROM ChatSession cs WHERE cs.createdAt BETWEEN :startDate AND :endDate")
    Long countSessionsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 평균 세션 지속 시간 조회 (분 단위)
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt)) FROM ChatSession cs WHERE cs.isActive = false")
    Double findAverageSessionDuration();

    /**
     * 사용자별 평균 세션 지속 시간 조회
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt)) FROM ChatSession cs " +
           "WHERE cs.user = :user AND cs.isActive = false")
    Double findAverageSessionDurationByUser(@Param("user") User user);

    /**
     * 가장 활발한 사용자 조회 (세션 수 기준)
     */
    @Query("SELECT cs.user, COUNT(cs) as sessionCount FROM ChatSession cs " +
           "GROUP BY cs.user ORDER BY sessionCount DESC")
    List<Object[]> findMostActiveUsers();

    /**
     * 최근 N일 내 활동한 사용자 조회
     */
    @Query("SELECT DISTINCT cs.user FROM ChatSession cs WHERE cs.endedAt >= :since")
    List<User> findRecentActiveUsers(@Param("since") LocalDateTime since);

    /**
     * 세션 통계 조회 (일별)
     */
    @Query("SELECT DATE(cs.createdAt) as date, COUNT(cs) as sessionCount " +
           "FROM ChatSession cs WHERE cs.createdAt >= :startDate " +
           "GROUP BY DATE(cs.createdAt) ORDER BY date DESC")
    List<Object[]> findDailySessionStats(@Param("startDate") LocalDateTime startDate);

    /**
     * 장기간 비활성 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.isActive = true " +
           "AND cs.endedAt < :cutoffTime ORDER BY cs.endedAt ASC")
    List<ChatSession> findLongInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 사용자의 활성 세션 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM ChatSession cs " +
           "WHERE cs.user = :user AND cs.isActive = true")
    Boolean hasActiveSession(@Param("user") User user);

    /**
     * 세션 ID 중복 확인
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END FROM ChatSession cs " +
           "WHERE cs.sessionId = :sessionId")
    Boolean existsBySessionId(@Param("sessionId") String sessionId);

    /**
     * 사용자의 마지막 활동 시간 조회
     */
    @Query("SELECT MAX(cs.endedAt) FROM ChatSession cs WHERE cs.user = :user")
    Optional<LocalDateTime> findLastActivityByUser(@Param("user") User user);

    /**
     * 세션 활동 패턴 분석 (시간대별)
     */
    @Query("SELECT HOUR(cs.createdAt) as hour, COUNT(cs) as sessionCount " +
           "FROM ChatSession cs WHERE cs.createdAt >= :startDate " +
           "GROUP BY HOUR(cs.createdAt) ORDER BY hour")
    List<Object[]> findHourlySessionPattern(@Param("startDate") LocalDateTime startDate);

    /**
     * 세션 길이별 분포 조회
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt) <= 5 THEN '5분 이하' " +
           "WHEN TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt) <= 15 THEN '6-15분' " +
           "WHEN TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt) <= 30 THEN '16-30분' " +
           "WHEN TIMESTAMPDIFF(MINUTE, cs.createdAt, cs.endedAt) <= 60 THEN '31-60분' " +
           "ELSE '60분 초과' " +
           "END as duration_range, " +
           "COUNT(cs) as session_count " +
           "FROM ChatSession cs WHERE cs.isActive = false " +
           "GROUP BY duration_range")
    List<Object[]> findSessionDurationDistribution();

    /**
     * 주간 활동 패턴 분석
     */
    @Query("SELECT DAYOFWEEK(cs.createdAt) as dayOfWeek, COUNT(cs) as sessionCount " +
           "FROM ChatSession cs WHERE cs.createdAt >= :startDate " +
           "GROUP BY DAYOFWEEK(cs.createdAt) ORDER BY dayOfWeek")
    List<Object[]> findWeeklySessionPattern(@Param("startDate") LocalDateTime startDate);

    /**
     * 세션 ID로 세션 조회 (활성/비활성 무관)
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.sessionId = :sessionId")
    Optional<ChatSession> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 사용자의 최근 N개 세션 조회
     */
    @Query("SELECT cs FROM ChatSession cs WHERE cs.user = :user " +
           "ORDER BY cs.createdAt DESC LIMIT :limit")
    List<ChatSession> findRecentSessionsByUser(@Param("user") User user, @Param("limit") Integer limit);
}