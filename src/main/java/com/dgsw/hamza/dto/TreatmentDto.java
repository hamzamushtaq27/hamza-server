package com.dgsw.hamza.dto;

import com.dgsw.hamza.enums.TreatmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "치료 관련 DTO")
public class TreatmentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료법 정보")
    public static class TreatmentInfo {
        
        @Schema(description = "치료법 ID", example = "1")
        private Long treatmentId;
        
        @Schema(description = "치료법 이름", example = "인지행동치료")
        private String name;
        
        @Schema(description = "치료법 설명", example = "부정적 사고 패턴을 인식하고 변화시키는 치료법")
        private String description;
        
        @Schema(description = "치료법 유형")
        private TreatmentType type;
        
        @Schema(description = "소요시간 (분)", example = "20")
        private Integer durationMinutes;
        
        @Schema(description = "난이도 레벨", example = "BEGINNER")
        private String difficultyLevel;
        
        @Schema(description = "활성화 여부", example = "true")
        private Boolean isActive;
        
        @Schema(description = "생성일시")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시")
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 프로그램 정보")
    public static class TreatmentProgramInfo {
        
        @Schema(description = "프로그램 ID", example = "1")
        private Long programId;
        
        @Schema(description = "프로그램 이름", example = "우울증 극복 프로그램")
        private String name;
        
        @Schema(description = "프로그램 설명", example = "단계별 CBT 기법을 활용한 우울증 치료 프로그램")
        private String description;
        
        @Schema(description = "프로그램 기간 (주)", example = "8")
        private Integer durationWeeks;
        
        @Schema(description = "주당 세션 수", example = "3")
        private Integer sessionsPerWeek;
        
        @Schema(description = "대상 질환", example = "DEPRESSION")
        private String targetCondition;
        
        @Schema(description = "활성화 여부", example = "true")
        private Boolean isActive;
        
        @Schema(description = "포함된 치료법 목록")
        private List<TreatmentInfo> treatments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 컨텐츠 정보")
    public static class TreatmentContentInfo {
        
        @Schema(description = "컨텐츠 ID", example = "1")
        private Long contentId;
        
        @Schema(description = "컨텐츠 제목", example = "CBT 기초 이해하기")
        private String title;
        
        @Schema(description = "컨텐츠 내용", example = "인지행동치료의 기본 개념과 원리를 학습합니다.")
        private String content;
        
        @Schema(description = "컨텐츠 유형", example = "TEXT")
        private String contentType;
        
        @Schema(description = "순서", example = "1")
        private Integer sequenceOrder;
        
        @Schema(description = "활성화 여부", example = "true")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료법 목록 조회 요청")
    public static class TreatmentListRequest {
        
        @Schema(description = "치료법 유형 필터")
        private TreatmentType type;
        
        @Schema(description = "난이도 레벨 필터", example = "BEGINNER")
        private String difficultyLevel;
        
        @Schema(description = "최대 소요시간 (분)", example = "30")
        private Integer maxDuration;
        
        @Schema(description = "활성화된 치료법만 조회", example = "true")
        private Boolean activeOnly;
        
        @Schema(description = "페이지 번호", example = "0")
        private Integer page;
        
        @Schema(description = "페이지 크기", example = "10")
        private Integer size;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료법 목록 응답")
    public static class TreatmentListResponse {
        
        @Schema(description = "치료법 목록")
        private List<TreatmentInfo> treatments;
        
        @Schema(description = "총 개수")
        private Long totalCount;
        
        @Schema(description = "현재 페이지")
        private Integer currentPage;
        
        @Schema(description = "총 페이지 수")
        private Integer totalPages;
        
        @Schema(description = "다음 페이지 존재 여부")
        private Boolean hasNext;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 유형별 통계")
    public static class TreatmentTypeStats {
        
        @Schema(description = "치료법 유형")
        private TreatmentType type;
        
        @Schema(description = "해당 유형의 치료법 수")
        private Integer count;
        
        @Schema(description = "평균 소요시간")
        private Double averageDuration;
        
        @Schema(description = "인기도 점수")
        private Double popularityScore;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 통계 응답")
    public static class TreatmentStatsResponse {
        
        @Schema(description = "총 치료법 수")
        private Integer totalTreatments;
        
        @Schema(description = "활성화된 치료법 수")
        private Integer activeTreatments;
        
        @Schema(description = "치료 유형별 통계")
        private List<TreatmentTypeStats> typeStats;
        
        @Schema(description = "평균 소요시간")
        private Double averageDuration;
        
        @Schema(description = "가장 인기 있는 치료법")
        private TreatmentInfo mostPopularTreatment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "치료 진행 상태")
    public static class TreatmentProgress {
        
        @Schema(description = "치료법 ID")
        private Long treatmentId;
        
        @Schema(description = "치료법 이름")
        private String treatmentName;
        
        @Schema(description = "완료된 세션 수")
        private Integer completedSessions;
        
        @Schema(description = "총 세션 수")
        private Integer totalSessions;
        
        @Schema(description = "진행률 (%)")
        private Double progressPercentage;
        
        @Schema(description = "마지막 세션 일시")
        private LocalDateTime lastSessionDate;
        
        @Schema(description = "다음 세션 예정일")
        private LocalDateTime nextSessionDate;
        
        @Schema(description = "치료 상태", example = "ACTIVE")
        private String status;
    }
}