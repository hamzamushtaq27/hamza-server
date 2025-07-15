package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.RecommendationDto;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.repository.UserRepository;
import com.dgsw.hamza.security.UserPrincipal;
import com.dgsw.hamza.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recommendation", description = "치료 추천 API")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    @Operation(summary = "진단 기반 치료 추천", description = "진단 결과를 기반으로 개인화된 치료법을 추천합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "치료 추천 성공",
                    content = @Content(schema = @Schema(implementation = RecommendationDto.RecommendationResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "진단을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/generate")
    public ResponseEntity<RecommendationDto.RecommendationResponse> generateRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RecommendationDto.RecommendationRequest request) {

        log.info("사용자 {} 치료 추천 생성 요청", userPrincipal.getId());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendationDto.RecommendationResponse response = 
                recommendationService.generateRecommendations(user, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "추천 히스토리 조회", description = "사용자의 과거 치료 추천 기록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 히스토리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/history")
    public ResponseEntity<RecommendationDto.RecommendationHistoryResponse> getRecommendationHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "조회할 추천 개수") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("사용자 {} 추천 히스토리 조회 (limit: {})", userPrincipal.getId(), limit);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendationDto.RecommendationHistoryResponse response = 
                recommendationService.getRecommendationHistory(user, limit);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "추천 통계 조회", description = "사용자의 치료 추천 통계를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/stats")
    public ResponseEntity<RecommendationDto.RecommendationStats> getRecommendationStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("사용자 {} 추천 통계 조회", userPrincipal.getId());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendationDto.RecommendationStats response = 
                recommendationService.getRecommendationStats(user);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "추천 상태 업데이트", description = "특정 추천의 상태를 업데이트합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 상태 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "추천을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/status")
    public ResponseEntity<Void> updateRecommendationStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RecommendationDto.RecommendationUpdateRequest request) {

        log.info("사용자 {} 추천 {} 상태 업데이트", userPrincipal.getId(), request.getRecommendationId());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        recommendationService.updateRecommendationStatus(user, request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 진단의 추천 결과 조회", description = "특정 진단에 대한 추천 결과를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 결과 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "진단을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/diagnosis/{diagnosisId}")
    public ResponseEntity<RecommendationDto.RecommendationCombinationResponse> getRecommendationByDiagnosis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "진단 ID") @PathVariable Long diagnosisId) {

        log.info("사용자 {} 진단 {} 추천 결과 조회", userPrincipal.getId(), diagnosisId);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        RecommendationDto.RecommendationCombinationResponse response = 
                recommendationService.getRecommendationByDiagnosis(user, diagnosisId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "추천 상태 목록", description = "사용 가능한 추천 상태 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 상태 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/statuses")
    public ResponseEntity<RecommendationStatusInfo[]> getRecommendationStatuses() {
        log.info("추천 상태 목록 조회 요청");
        
        RecommendationStatusInfo[] statuses = {
                new RecommendationStatusInfo("PENDING", "대기 중", "아직 시작하지 않은 추천"),
                new RecommendationStatusInfo("ACTIVE", "진행 중", "현재 진행 중인 추천"),
                new RecommendationStatusInfo("COMPLETED", "완료", "완료된 추천"),
                new RecommendationStatusInfo("CANCELLED", "취소", "취소된 추천")
        };
        
        return ResponseEntity.ok(statuses);
    }

    // Inner class for recommendation status info
    @Schema(description = "추천 상태 정보")
    public static class RecommendationStatusInfo {
        @Schema(description = "상태 코드")
        private String code;
        
        @Schema(description = "상태 이름")
        private String name;
        
        @Schema(description = "상태 설명")
        private String description;

        public RecommendationStatusInfo(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        // Getters
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}