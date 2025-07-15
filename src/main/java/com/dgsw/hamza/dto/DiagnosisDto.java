package com.dgsw.hamza.dto;

import com.dgsw.hamza.enums.DiagnosisSeverity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "진단 관련 DTO")
public class DiagnosisDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "진단 설문 제출 요청")
    public static class DiagnosisSubmitRequest {
        
        @NotNull(message = "답변 목록은 필수입니다")
        @Schema(description = "PHQ-9 설문 답변 목록")
        private List<DiagnosisAnswerRequest> answers;
        
        @Schema(description = "추가 메모", example = "최근 스트레스가 많았습니다")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "개별 설문 답변")
    public static class DiagnosisAnswerRequest {
        
        @NotNull(message = "문항 ID는 필수입니다")
        @Schema(description = "문항 ID", example = "1")
        private Long questionId;
        
        @NotNull(message = "답변 점수는 필수입니다")
        @Min(value = 0, message = "답변 점수는 0 이상이어야 합니다")
        @Max(value = 3, message = "답변 점수는 3 이하여야 합니다")
        @Schema(description = "답변 점수 (0: 전혀 없음, 1: 며칠 동안, 2: 일주일 이상, 3: 거의 매일)", example = "2")
        private Integer score;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "진단 결과 응답")
    public static class DiagnosisResponse {
        
        @Schema(description = "진단 ID", example = "1")
        private Long diagnosisId;
        
        @Schema(description = "총 점수", example = "12")
        private Integer totalScore;
        
        @Schema(description = "심각도 레벨")
        private DiagnosisSeverity severity;
        
        @Schema(description = "심각도 설명", example = "중등도 - 중간 정도의 우울증 증상이 있습니다")
        private String severityDescription;
        
        @Schema(description = "권장사항", example = "전문가 상담을 받아보시기 바랍니다")
        private String recommendation;
        
        @Schema(description = "심각도 색상", example = "#FF9800")
        private String severityColor;
        
        @Schema(description = "진단 일시")
        private LocalDateTime diagnosisDate;
        
        @Schema(description = "이전 진단과의 비교", example = "이전 진단보다 2점 낮아졌습니다")
        private String scoreComparison;
        
        @Schema(description = "긴급 처리 필요 여부")
        private Boolean requiresImmediateAttention;
        
        @Schema(description = "다음 진단 권장 일수", example = "7")
        private Integer recommendedRediagnosisDays;
        
        @Schema(description = "메모", example = "최근 스트레스가 많았습니다")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "진단 히스토리 응답")
    public static class DiagnosisHistoryResponse {
        
        @Schema(description = "진단 목록")
        private List<DiagnosisHistoryItem> diagnoses;
        
        @Schema(description = "총 개수")
        private Integer totalCount;
        
        @Schema(description = "평균 점수")
        private Double averageScore;
        
        @Schema(description = "최고 점수")
        private Integer maxScore;
        
        @Schema(description = "최저 점수")
        private Integer minScore;
        
        @Schema(description = "개선 여부")
        private Boolean isImproving;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "진단 히스토리 항목")
    public static class DiagnosisHistoryItem {
        
        @Schema(description = "진단 ID")
        private Long diagnosisId;
        
        @Schema(description = "총 점수")
        private Integer totalScore;
        
        @Schema(description = "심각도 레벨")
        private DiagnosisSeverity severity;
        
        @Schema(description = "진단 일시")
        private LocalDateTime diagnosisDate;
        
        @Schema(description = "이전 진단과의 점수 차이")
        private Integer scoreDifference;
        
        @Schema(description = "메모")
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "재진단 필요 여부 응답")
    public static class RedoCheckResponse {
        
        @Schema(description = "재진단 필요 여부")
        private Boolean isRedoRequired;
        
        @Schema(description = "마지막 진단 이후 경과 일수")
        private Integer daysSinceLastDiagnosis;
        
        @Schema(description = "마지막 진단 심각도")
        private DiagnosisSeverity lastSeverity;
        
        @Schema(description = "권장 재진단 주기")
        private Integer recommendedDays;
        
        @Schema(description = "메시지")
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "PHQ-9 설문 문항 응답")
    public static class QuestionResponse {
        
        @Schema(description = "문항 ID")
        private Long questionId;
        
        @Schema(description = "문항 텍스트")
        private String questionText;
        
        @Schema(description = "문항 순서")
        private Integer questionOrder;
        
        @Schema(description = "활성화 여부")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "진단 통계 응답")
    public static class DiagnosisStatsResponse {
        
        @Schema(description = "총 진단 수")
        private Integer totalDiagnoses;
        
        @Schema(description = "평균 점수")
        private Double averageScore;
        
        @Schema(description = "최근 30일 진단 수")
        private Integer recentDiagnoses;
        
        @Schema(description = "가장 빈번한 심각도")
        private DiagnosisSeverity mostFrequentSeverity;
        
        @Schema(description = "심각도별 분포")
        private List<SeverityDistribution> severityDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "심각도 분포")
    public static class SeverityDistribution {
        
        @Schema(description = "심각도 레벨")
        private DiagnosisSeverity severity;
        
        @Schema(description = "해당 심각도 진단 수")
        private Integer count;
        
        @Schema(description = "전체 대비 비율")
        private Double percentage;
    }
}