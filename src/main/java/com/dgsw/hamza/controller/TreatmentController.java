package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.TreatmentDto;
import com.dgsw.hamza.enums.TreatmentType;
import com.dgsw.hamza.service.TreatmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Treatment", description = "치료법 관리 API")
public class TreatmentController {

    private final TreatmentService treatmentService;

    @Operation(summary = "활성화된 치료법 목록 조회", description = "현재 활성화된 모든 치료법을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> getActiveTreatments() {
        log.info("활성화된 치료법 목록 조회 요청");
        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.getActiveTreatments();
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "치료법 목록 조회 (필터링)", description = "필터링 조건에 따라 치료법 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<TreatmentDto.TreatmentListResponse> getTreatmentList(
            @Parameter(description = "치료법 유형") @RequestParam(required = false) TreatmentType type,
            @Parameter(description = "난이도 레벨") @RequestParam(required = false) String difficultyLevel,
            @Parameter(description = "최대 소요시간 (분)") @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "활성화된 치료법만 조회") @RequestParam(defaultValue = "true") Boolean activeOnly,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") Integer size) {

        log.info("치료법 목록 조회 - 유형: {}, 난이도: {}, 최대시간: {}", type, difficultyLevel, maxDuration);

        TreatmentDto.TreatmentListRequest request = TreatmentDto.TreatmentListRequest.builder()
                .type(type)
                .difficultyLevel(difficultyLevel)
                .maxDuration(maxDuration)
                .activeOnly(activeOnly)
                .page(page)
                .size(size)
                .build();

        TreatmentDto.TreatmentListResponse response = treatmentService.getTreatmentList(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "치료법 상세 조회", description = "특정 치료법의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "치료법을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{treatmentId}")
    public ResponseEntity<TreatmentDto.TreatmentInfo> getTreatmentDetail(
            @Parameter(description = "치료법 ID") @PathVariable Long treatmentId) {

        log.info("치료법 상세 조회 - ID: {}", treatmentId);
        TreatmentDto.TreatmentInfo treatment = treatmentService.getTreatmentDetail(treatmentId);
        return ResponseEntity.ok(treatment);
    }

    @Operation(summary = "치료법 유형별 조회", description = "특정 유형의 치료법들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 유형별 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 치료법 유형"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> getTreatmentsByType(
            @Parameter(description = "치료법 유형") @PathVariable TreatmentType type) {

        log.info("치료법 유형별 조회 - 유형: {}", type);
        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.getTreatmentsByType(type);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "치료법 검색", description = "키워드를 이용해 치료법을 검색합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색 키워드가 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search/keyword")
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> searchTreatments(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {

        log.info("치료법 검색 - 키워드: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.searchTreatments(keyword);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "치료법 통계", description = "치료법 관련 통계 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/stats")
    public ResponseEntity<TreatmentDto.TreatmentStatsResponse> getTreatmentStats() {
        log.info("치료법 통계 조회 요청");
        TreatmentDto.TreatmentStatsResponse stats = treatmentService.getTreatmentStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "치료법 컨텐츠 조회", description = "특정 치료법의 컨텐츠를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 컨텐츠 조회 성공"),
            @ApiResponse(responseCode = "404", description = "치료법을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{treatmentId}/contents")
    public ResponseEntity<List<TreatmentDto.TreatmentContentInfo>> getTreatmentContents(
            @Parameter(description = "치료법 ID") @PathVariable Long treatmentId) {

        log.info("치료법 컨텐츠 조회 - 치료법 ID: {}", treatmentId);
        List<TreatmentDto.TreatmentContentInfo> contents = treatmentService.getTreatmentContents(treatmentId);
        return ResponseEntity.ok(contents);
    }

    @Operation(summary = "인기 치료법 조회", description = "추천 받은 횟수가 많은 인기 치료법을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 치료법 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/popular")
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> getPopularTreatments(
            @Parameter(description = "조회할 개수") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("인기 치료법 조회 - limit: {}", limit);
        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.getPopularTreatments(limit);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "소요시간별 치료법 조회", description = "지정된 시간 이하의 치료법들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "소요시간별 치료법 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 시간 값"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/duration/{maxDuration}")
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> getTreatmentsByDuration(
            @Parameter(description = "최대 소요시간 (분)") @PathVariable Integer maxDuration) {

        log.info("소요시간별 치료법 조회 - 최대 {}분", maxDuration);
        
        if (maxDuration <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.getTreatmentsByDuration(maxDuration);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "난이도별 치료법 조회", description = "특정 난이도의 치료법들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "난이도별 치료법 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 난이도 값"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/difficulty/{level}")
    public ResponseEntity<List<TreatmentDto.TreatmentInfo>> getTreatmentsByDifficulty(
            @Parameter(description = "난이도 레벨 (BEGINNER, INTERMEDIATE, ADVANCED)") @PathVariable String level) {

        log.info("난이도별 치료법 조회 - 난이도: {}", level);
        
        if (!isValidDifficultyLevel(level)) {
            return ResponseEntity.badRequest().build();
        }

        List<TreatmentDto.TreatmentInfo> treatments = treatmentService.getTreatmentsByDifficulty(level);
        return ResponseEntity.ok(treatments);
    }

    @Operation(summary = "치료법 유형 목록", description = "사용 가능한 치료법 유형 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료법 유형 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/types")
    public ResponseEntity<List<TreatmentTypeInfo>> getTreatmentTypes() {
        log.info("치료법 유형 목록 조회 요청");
        
        List<TreatmentTypeInfo> types = List.of(
                new TreatmentTypeInfo(TreatmentType.CBT, TreatmentType.CBT.getDisplayName(), TreatmentType.CBT.getDescription()),
                new TreatmentTypeInfo(TreatmentType.MEDITATION, TreatmentType.MEDITATION.getDisplayName(), TreatmentType.MEDITATION.getDescription()),
                new TreatmentTypeInfo(TreatmentType.RELAXATION, TreatmentType.RELAXATION.getDisplayName(), TreatmentType.RELAXATION.getDescription()),
                new TreatmentTypeInfo(TreatmentType.EXERCISE, TreatmentType.EXERCISE.getDisplayName(), TreatmentType.EXERCISE.getDescription()),
                new TreatmentTypeInfo(TreatmentType.MEDICATION, TreatmentType.MEDICATION.getDisplayName(), TreatmentType.MEDICATION.getDescription())
        );
        
        return ResponseEntity.ok(types);
    }

    // Helper methods
    private boolean isValidDifficultyLevel(String level) {
        return "BEGINNER".equals(level) || "INTERMEDIATE".equals(level) || "ADVANCED".equals(level);
    }

    // Inner class for treatment type info
    @Schema(description = "치료법 유형 정보")
    public static class TreatmentTypeInfo {
        @Schema(description = "치료법 유형")
        private TreatmentType type;
        
        @Schema(description = "표시 이름")
        private String displayName;
        
        @Schema(description = "설명")
        private String description;

        public TreatmentTypeInfo(TreatmentType type, String displayName, String description) {
            this.type = type;
            this.displayName = displayName;
            this.description = description;
        }

        // Getters
        public TreatmentType getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}