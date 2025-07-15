package com.dgsw.hamza.dto;

import com.dgsw.hamza.enums.DiagnosisSeverity;
import com.dgsw.hamza.enums.RecommendationPriority;
import com.dgsw.hamza.enums.TreatmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "치료 추천 관련 DTO")
public class RecommendationDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 추천 요청")
    public static class RecommendationRequest {
        
        @Schema(description = "진단 ID", example = "1")
        private Long diagnosisId;
        
        @Schema(description = "최대 추천 수", example = "5")
        private Integer maxRecommendations;
        
        @Schema(description = "치료법 유형 필터")
        private List<TreatmentType> includeTypes;
        
        @Schema(description = "제외할 치료법 유형")
        private List<TreatmentType> excludeTypes;
        
        @Schema(description = "최대 소요시간 (분)", example = "30")
        private Integer maxDuration;
        
        @Schema(description = "개인화 추천 적용", example = "true")
        private Boolean personalizeRecommendation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "개별 치료 추천 정보")
    public static class TreatmentRecommendationInfo {
        
        @Schema(description = "치료법 정보")
        private TreatmentDto.TreatmentInfo treatment;
        
        @Schema(description = "추천 점수", example = "0.85")
        private Double recommendationScore;
        
        @Schema(description = "추천 우선순위")
        private RecommendationPriority priority;
        
        @Schema(description = "추천 이유", example = "중등도 우울 증상 치료를 위해 부정적 사고 패턴을 개선하는 인지행동치료를 권장합니다.")
        private String reason;
        
        @Schema(description = "예상 효과", example = "인지적 왜곡을 교정하여 우울 증상을 완화시킵니다.")
        private String expectedEffect;
        
        @Schema(description = "주의사항", example = "정기적인 연습이 필요합니다.")
        private String precautions;
        
        @Schema(description = "관련 컨텐츠")
        private List<TreatmentDto.TreatmentContentInfo> contents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 추천 응답")
    public static class RecommendationResponse {
        
        @Schema(description = "추천 ID", example = "1")
        private Long recommendationId;
        
        @Schema(description = "진단 ID", example = "1")
        private Long diagnosisId;
        
        @Schema(description = "진단 심각도")
        private DiagnosisSeverity diagnosisSeverity;
        
        @Schema(description = "추천된 치료법 목록")
        private List<TreatmentRecommendationInfo> recommendations;
        
        @Schema(description = "추천 생성 일시")
        private LocalDateTime recommendationDate;
        
        @Schema(description = "추천 상태", example = "ACTIVE")
        private String status;
        
        @Schema(description = "총 추천 수", example = "5")
        private Integer totalRecommendations;
        
        @Schema(description = "긴급 추천 포함 여부", example = "false")
        private Boolean hasUrgentRecommendations;
        
        @Schema(description = "추천 요약", example = "중등도 우울 증상에 대한 5가지 치료법을 추천합니다.")
        private String summary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 히스토리 항목")
    public static class RecommendationHistoryItem {
        
        @Schema(description = "추천 ID")
        private Long recommendationId;
        
        @Schema(description = "진단 ID")
        private Long diagnosisId;
        
        @Schema(description = "진단 심각도")
        private DiagnosisSeverity diagnosisSeverity;
        
        @Schema(description = "추천 수")
        private Integer recommendationCount;
        
        @Schema(description = "추천 일시")
        private LocalDateTime recommendationDate;
        
        @Schema(description = "추천 상태")
        private String status;
        
        @Schema(description = "실행된 치료법 수")
        private Integer executedCount;
        
        @Schema(description = "완료된 치료법 수")
        private Integer completedCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 히스토리 응답")
    public static class RecommendationHistoryResponse {
        
        @Schema(description = "추천 히스토리 목록")
        private List<RecommendationHistoryItem> recommendations;
        
        @Schema(description = "총 추천 수")
        private Long totalCount;
        
        @Schema(description = "평균 추천 점수")
        private Double averageScore;
        
        @Schema(description = "가장 많이 추천된 치료법 유형")
        private TreatmentType mostRecommendedType;
        
        @Schema(description = "추천 효과성 점수")
        private Double effectivenessScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 통계")
    public static class RecommendationStats {
        
        @Schema(description = "총 추천 수")
        private Integer totalRecommendations;
        
        @Schema(description = "실행된 추천 수")
        private Integer executedRecommendations;
        
        @Schema(description = "완료된 추천 수")
        private Integer completedRecommendations;
        
        @Schema(description = "추천 실행률 (%)")
        private Double executionRate;
        
        @Schema(description = "추천 완료율 (%)")
        private Double completionRate;
        
        @Schema(description = "치료법 유형별 추천 분포")
        private List<TypeRecommendationStats> typeStats;
        
        @Schema(description = "우선순위별 추천 분포")
        private List<PriorityRecommendationStats> priorityStats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료법 유형별 추천 통계")
    public static class TypeRecommendationStats {
        
        @Schema(description = "치료법 유형")
        private TreatmentType type;
        
        @Schema(description = "추천 수")
        private Integer count;
        
        @Schema(description = "평균 추천 점수")
        private Double averageScore;
        
        @Schema(description = "실행률 (%)")
        private Double executionRate;
        
        @Schema(description = "완료율 (%)")
        private Double completionRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "우선순위별 추천 통계")
    public static class PriorityRecommendationStats {
        
        @Schema(description = "우선순위")
        private RecommendationPriority priority;
        
        @Schema(description = "추천 수")
        private Integer count;
        
        @Schema(description = "평균 추천 점수")
        private Double averageScore;
        
        @Schema(description = "실행률 (%)")
        private Double executionRate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 업데이트 요청")
    public static class RecommendationUpdateRequest {
        
        @Schema(description = "추천 ID")
        private Long recommendationId;
        
        @Schema(description = "치료법 ID")
        private Long treatmentId;
        
        @Schema(description = "실행 상태", example = "STARTED")
        private String status;
        
        @Schema(description = "피드백", example = "치료법이 도움이 되었습니다.")
        private String feedback;
        
        @Schema(description = "평점 (1-5)", example = "4")
        private Integer rating;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 조합 응답")
    public static class RecommendationCombinationResponse {
        
        @Schema(description = "주요 치료법 (1순위)")
        private TreatmentRecommendationInfo primaryTreatment;
        
        @Schema(description = "보조 치료법 목록")
        private List<TreatmentRecommendationInfo> supportingTreatments;
        
        @Schema(description = "조합 점수")
        private Double combinationScore;
        
        @Schema(description = "조합 설명")
        private String combinationDescription;
        
        @Schema(description = "예상 치료 기간 (주)")
        private Integer expectedDurationWeeks;
        
        @Schema(description = "주의사항")
        private List<String> precautions;
    }
}