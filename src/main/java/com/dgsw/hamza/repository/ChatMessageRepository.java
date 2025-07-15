package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.ChatMessage;
import com.dgsw.hamza.entity.ChatSession;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.ChatMessageType;
import com.dgsw.hamza.enums.CrisisLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 세션의 메시지 조회 (시간 순)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByChatSessionOrderByCreatedAt(@Param("chatSession") ChatSession chatSession);

    /**
     * 세션의 메시지 조회 (페이징)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByChatSessionOrderByCreatedAtDesc(@Param("chatSession") ChatSession chatSession, Pageable pageable);

    /**
     * 사용자의 모든 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    /**
     * 특정 세션의 최근 N개 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt DESC LIMIT :limit")
    List<ChatMessage> findRecentMessagesByChatSession(@Param("chatSession") ChatSession chatSession, @Param("limit") Integer limit);

    /**
     * 위기 상황 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.isCrisisRelated = true ORDER BY cm.createdAt DESC")
    List<ChatMessage> findCrisisMessages();

    /**
     * 사용자의 위기 상황 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user AND cm.isCrisisRelated = true ORDER BY cm.createdAt DESC")
    List<ChatMessage> findCrisisMessagesByUser(@Param("user") User user);

    /**
     * 특정 기간 내 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByUserAndDateRange(@Param("user") User user,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 메시지 타입별 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.messageType = :messageType " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByMessageType(@Param("messageType") ChatMessageType messageType);

    /**
     * 사용자의 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * 세션의 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession = :chatSession")
    Long countByChatSession(@Param("chatSession") ChatSession chatSession);

    /**
     * 사용자의 위기 상황 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession.user = :user AND cm.isCrisisRelated = true")
    Long countCrisisMessagesByUser(@Param("user") User user);

    /**
     * 오늘 생성된 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE DATE(cm.createdAt) = DATE(NOW())")
    Long countTodayMessages();

    /**
     * 특정 기간 내 메시지 수 조회
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.createdAt BETWEEN :startDate AND :endDate")
    Long countMessagesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 위기 수준별 메시지 분포 조회
     */
    @Query("SELECT cm.isCrisisRelated, COUNT(cm) FROM ChatMessage cm WHERE cm.chatSession.user = :user GROUP BY cm.isCrisisRelated")
    List<Object[]> findCrisisRelatedDistribution(@Param("user") User user);

    /**
     * 메시지 타입별 분포 조회
     */
    @Query("SELECT cm.messageType, COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.chatSession.user = :user GROUP BY cm.messageType")
    List<Object[]> findMessageTypeDistribution(@Param("user") User user);

    /**
     * 사용자의 최근 대화 컨텍스트 조회
     */
    @Query("SELECT cm.messageContent FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.messageType = 'USER' ORDER BY cm.createdAt DESC LIMIT :limit")
    List<String> findRecentUserMessages(@Param("user") User user, @Param("limit") Integer limit);

    /**
     * 키워드 기반 메시지 검색
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND LOWER(cm.messageContent) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findByUserAndContentContaining(@Param("user") User user,
                                                     @Param("keyword") String keyword);

    /**
     * 감정 점수 기반 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.emotionScore IS NOT NULL " +
           "ORDER BY cm.emotionScore ASC")
    List<ChatMessage> findByUserOrderByEmotionScore(@Param("user") User user);

    /**
     * 평균 감정 점수 조회
     */
    @Query("SELECT AVG(cm.emotionScore) FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.emotionScore IS NOT NULL")
    Double findAverageEmotionScoreByUser(@Param("user") User user);

    /**
     * 최근 N일 내 감정 점수 변화 조회
     */
    @Query("SELECT DATE(cm.createdAt) as date, AVG(cm.emotionScore) as avgScore " +
           "FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.emotionScore IS NOT NULL " +
           "AND cm.createdAt >= :since " +
           "GROUP BY DATE(cm.createdAt) ORDER BY date DESC")
    List<Object[]> findEmotionScoreTrend(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * 위기 상황 패턴 분석 (시간대별)
     */
    @Query("SELECT HOUR(cm.createdAt) as hour, COUNT(cm) as crisisCount " +
           "FROM ChatMessage cm WHERE cm.isCrisisRelated = true " +
           "AND cm.createdAt >= :startDate " +
           "GROUP BY HOUR(cm.createdAt) ORDER BY hour")
    List<Object[]> findCrisisPatternByHour(@Param("startDate") LocalDateTime startDate);

    /**
     * 연속 위기 상황 감지
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.messageType = 'USER' " +
           "ORDER BY cm.createdAt DESC LIMIT :limit")
    List<ChatMessage> findRecentUserMessagesForCrisisDetection(@Param("user") User user,
                                                               @Param("limit") Integer limit);

    /**
     * 세션별 위기 상황 통계
     */
    @Query("SELECT cm.chatSession, COUNT(cm) as crisisCount FROM ChatMessage cm WHERE cm.isCrisisRelated = true GROUP BY cm.chatSession ORDER BY crisisCount DESC")
    List<Object[]> findCrisisStatsBySession();

    /**
     * 긴급 상황 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.isCrisisRelated = true " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findCriticalMessages();

    /**
     * 사용자별 긴급 상황 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.isCrisisRelated = true ORDER BY cm.createdAt DESC")
    List<ChatMessage> findCriticalMessagesByUser(@Param("user") User user);

    /**
     * 메시지 감정 분석 통계
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN cm.emotionScore > 0 THEN 1 END) as positiveCount, " +
           "COUNT(CASE WHEN cm.emotionScore < 0 THEN 1 END) as negativeCount, " +
           "COUNT(CASE WHEN cm.emotionScore = 0 THEN 1 END) as neutralCount " +
           "FROM ChatMessage cm WHERE cm.chatSession.user = :user AND cm.emotionScore IS NOT NULL")
    Object[] findEmotionStatsByUser(@Param("user") User user);

    /**
     * 대화 주제 분석을 위한 키워드 빈도 조회
     */
    @Query("SELECT cm.messageContent FROM ChatMessage cm WHERE cm.chatSession.user = :user " +
           "AND cm.messageType = 'USER' " +
           "AND cm.createdAt >= :since " +
           "ORDER BY cm.createdAt DESC")
    List<String> findRecentUserMessagesForTopicAnalysis(@Param("user") User user,
                                                        @Param("since") LocalDateTime since);

    /**
     * 봇 응답 효과성 분석
     */
    @Query("SELECT AVG(CASE WHEN next_cm.emotionScore > cm.emotionScore THEN 1 ELSE 0 END) " +
           "FROM ChatMessage cm " +
           "JOIN ChatMessage next_cm ON next_cm.chatSession = cm.chatSession " +
           "AND next_cm.createdAt > cm.createdAt " +
           "WHERE cm.messageType = 'BOT' AND cm.chatSession.user = :user " +
           "AND cm.emotionScore IS NOT NULL AND next_cm.emotionScore IS NOT NULL")
    Double findBotEffectivenessByUser(@Param("user") User user);

    /**
     * 최근 메시지 ID 조회
     */
    @Query("SELECT cm.id FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt DESC LIMIT 1")
    Optional<Long> findLatestMessageIdByChatSession(@Param("chatSession") ChatSession chatSession);

    /**
     * 세션의 첫 번째 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt ASC LIMIT 1")
    Optional<ChatMessage> findFirstMessageByChatSession(@Param("chatSession") ChatSession chatSession);

    /**
     * 세션의 마지막 메시지 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession = :chatSession ORDER BY cm.createdAt DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageByChatSession(@Param("chatSession") ChatSession chatSession);
}