package com.dgsw.hamza.dto;

import com.dgsw.hamza.enums.ChatMessageType;
import com.dgsw.hamza.enums.CrisisLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "채팅 관련 DTO")
public class ChatDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 메시지 전송 요청")
    public static class ChatMessageRequest {
        
        @NotBlank(message = "메시지 내용은 필수입니다")
        @Size(max = 1000, message = "메시지는 1000자 이내여야 합니다")
        @Schema(description = "메시지 내용", example = "안녕하세요, 요즘 기분이 좋지 않아요")
        private String message;
        
        @Schema(description = "세션 ID", example = "session_123")
        private String sessionId;
        
        @Schema(description = "긴급 상황 여부", example = "false")
        private Boolean isEmergency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 메시지 응답")
    public static class ChatMessageResponse {
        
        @Schema(description = "메시지 ID", example = "1")
        private Long messageId;
        
        @Schema(description = "세션 ID", example = "session_123")
        private String sessionId;
        
        @Schema(description = "봇 응답 메시지", example = "안녕하세요! 오늘 기분이 어떠신가요?")
        private String response;
        
        @Schema(description = "메시지 타입")
        private ChatMessageType messageType;
        
        @Schema(description = "위기 수준")
        private CrisisLevel crisisLevel;
        
        @Schema(description = "위기 상황 감지 여부", example = "false")
        private Boolean crisisDetected;
        
        @Schema(description = "추천 행동", example = "깊게 숨을 들이마시고 천천히 내쉬어보세요")
        private String recommendedAction;
        
        @Schema(description = "응급 연락처", example = "생명의전화: 1588-9191")
        private String emergencyContacts;
        
        @Schema(description = "관련 치료법 추천")
        private List<String> treatmentSuggestions;
        
        @Schema(description = "메시지 생성 시간")
        private LocalDateTime timestamp;
        
        @Schema(description = "감정 분석 결과")
        private EmotionAnalysisResult emotionAnalysis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 세션 정보")
    public static class ChatSessionInfo {
        
        @Schema(description = "세션 ID", example = "session_123")
        private String sessionId;
        
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;
        
        @Schema(description = "세션 시작 시간")
        private LocalDateTime startTime;
        
        @Schema(description = "마지막 활동 시간")
        private LocalDateTime lastActivity;
        
        @Schema(description = "메시지 개수", example = "10")
        private Integer messageCount;
        
        @Schema(description = "위기 상황 감지 횟수", example = "0")
        private Integer crisisDetectionCount;
        
        @Schema(description = "세션 활성 상태", example = "true")
        private Boolean isActive;
        
        @Schema(description = "세션 요약", example = "사용자와 일반적인 대화를 나눴습니다")
        private String sessionSummary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 히스토리 조회 요청")
    public static class ChatHistoryRequest {
        
        @Schema(description = "세션 ID", example = "session_123")
        private String sessionId;
        
        @Schema(description = "조회할 메시지 수", example = "50")
        private Integer limit;
        
        @Schema(description = "시작 날짜", example = "2024-01-01")
        private String startDate;
        
        @Schema(description = "끝 날짜", example = "2024-12-31")
        private String endDate;
        
        @Schema(description = "위기 상황 메시지만 조회", example = "false")
        private Boolean crisisOnly;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 히스토리 응답")
    public static class ChatHistoryResponse {
        
        @Schema(description = "메시지 목록")
        private List<ChatMessageHistory> messages;
        
        @Schema(description = "총 메시지 수", example = "25")
        private Integer totalCount;
        
        @Schema(description = "세션 정보")
        private ChatSessionInfo sessionInfo;
        
        @Schema(description = "위기 상황 감지 통계")
        private CrisisStatistics crisisStats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "채팅 메시지 히스토리")
    public static class ChatMessageHistory {
        
        @Schema(description = "메시지 ID", example = "1")
        private Long messageId;
        
        @Schema(description = "메시지 내용", example = "안녕하세요")
        private String content;
        
        @Schema(description = "메시지 타입")
        private ChatMessageType messageType;
        
        @Schema(description = "위기 수준")
        private CrisisLevel crisisLevel;
        
        @Schema(description = "메시지 시간")
        private LocalDateTime timestamp;
        
        @Schema(description = "감정 분석 결과")
        private EmotionAnalysisResult emotionAnalysis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "감정 분석 결과")
    public static class EmotionAnalysisResult {
        
        @Schema(description = "감정 유형", example = "긍정적")
        private String emotionType;
        
        @Schema(description = "감정 점수", example = "2")
        private Integer emotionScore;
        
        @Schema(description = "위기 수준")
        private CrisisLevel crisisLevel;
        
        @Schema(description = "부정적 감정 여부", example = "false")
        private Boolean isNegative;
        
        @Schema(description = "긍정적 감정 여부", example = "true")
        private Boolean isPositive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "위기 상황 통계")
    public static class CrisisStatistics {
        
        @Schema(description = "총 메시지 수", example = "100")
        private Integer totalMessages;
        
        @Schema(description = "위기 상황 메시지 수", example = "5")
        private Integer crisisMessages;
        
        @Schema(description = "최고 위기 수준")
        private CrisisLevel highestCrisisLevel;
        
        @Schema(description = "위기 상황 감지율 (%)", example = "5.0")
        private Double crisisRate;
        
        @Schema(description = "위기 수준별 분포")
        private List<CrisisLevelDistribution> crisisDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "위기 수준별 분포")
    public static class CrisisLevelDistribution {
        
        @Schema(description = "위기 수준")
        private CrisisLevel level;
        
        @Schema(description = "해당 수준 메시지 수", example = "3")
        private Integer count;
        
        @Schema(description = "전체 대비 비율 (%)", example = "3.0")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "챗봇 응답 설정")
    public static class ChatbotSettings {
        
        @Schema(description = "응답 언어", example = "ko")
        private String language;
        
        @Schema(description = "응답 톤", example = "friendly")
        private String tone;
        
        @Schema(description = "위기 상황 민감도", example = "medium")
        private String crisisSensitivity;
        
        @Schema(description = "자동 치료 추천", example = "true")
        private Boolean autoTreatmentRecommendation;
        
        @Schema(description = "응급 연락처 자동 제공", example = "true")
        private Boolean autoEmergencyContacts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "챗봇 상태 정보")
    public static class ChatbotStatus {
        
        @Schema(description = "챗봇 활성 상태", example = "true")
        private Boolean isActive;
        
        @Schema(description = "현재 활성 세션 수", example = "15")
        private Integer activeSessionCount;
        
        @Schema(description = "오늘 처리한 메시지 수", example = "234")
        private Integer todayMessageCount;
        
        @Schema(description = "위기 상황 감지 횟수", example = "3")
        private Integer crisisDetectionCount;
        
        @Schema(description = "평균 응답 시간 (ms)", example = "150")
        private Long averageResponseTime;
        
        @Schema(description = "마지막 업데이트 시간")
        private LocalDateTime lastUpdateTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "대화 컨텍스트 정보")
    public static class ConversationContext {
        
        @Schema(description = "세션 ID")
        private String sessionId;
        
        @Schema(description = "이전 메시지들")
        private List<String> previousMessages;
        
        @Schema(description = "현재 대화 주제", example = "우울감")
        private String currentTopic;
        
        @Schema(description = "사용자 감정 상태", example = "약간 부정적")
        private String userEmotionState;
        
        @Schema(description = "지속적인 위기 상황 여부", example = "false")
        private Boolean continuousCrisis;
        
        @Schema(description = "추천된 치료법 목록")
        private List<String> recommendedTreatments;
    }
}