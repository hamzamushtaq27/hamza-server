package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.DiagnosisDto;
import com.dgsw.hamza.dto.PageRequest;
import com.dgsw.hamza.dto.PageResponse;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.security.UserPrincipal;
import com.dgsw.hamza.repository.UserRepository;
import com.dgsw.hamza.service.DiagnosisService;
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

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Diagnosis", description = "PHQ-9 진단 API")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final UserRepository userRepository;

    @Operation(summary = "PHQ-9 설문 문항 조회", description = "진단에 사용될 PHQ-9 설문 문항들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "설문 문항 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/questions")
    public ResponseEntity<List<DiagnosisDto.QuestionResponse>> getQuestions() {
        log.info("PHQ-9 설문 문항 조회 요청");
        List<DiagnosisDto.QuestionResponse> questions = diagnosisService.getQuestions();
        return ResponseEntity.ok(questions);
    }

    @Operation(summary = "진단 설문 제출", description = "PHQ-9 설문 답변을 제출하고 진단 결과를 받습니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "진단 완료",
                    content = @Content(schema = @Schema(implementation = DiagnosisDto.DiagnosisResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/submit")
    public ResponseEntity<DiagnosisDto.DiagnosisResponse> submitDiagnosis(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DiagnosisDto.DiagnosisSubmitRequest request) {
        
        log.info("사용자 {} 진단 설문 제출", userPrincipal.getId());
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        DiagnosisDto.DiagnosisResponse response = diagnosisService.submitDiagnosis(user, request);
        
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "진단 히스토리 조회", description = "사용자의 과거 진단 기록을 페이지네이션으로 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "히스토리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/history")
    public ResponseEntity<PageResponse<DiagnosisDto.DiagnosisHistoryItem>> getDiagnosisHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String direction) {
        
        log.info("사용자 {} 진단 히스토리 조회 (page: {}, size: {})", userPrincipal.getId(), page, size);
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        PageRequest pageRequest = new PageRequest(page, size, sort, direction);
        PageResponse<DiagnosisDto.DiagnosisHistoryItem> response = 
            diagnosisService.getDiagnosisHistoryPaged(user, pageRequest);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "재진단 필요 여부 확인", description = "마지막 진단 이후 재진단이 필요한지 확인합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재진단 필요 여부 확인 완료"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/redo-check")
    public ResponseEntity<DiagnosisDto.RedoCheckResponse> checkRedoRequired(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("사용자 {} 재진단 필요 여부 확인", userPrincipal.getId());
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 간단한 재진단 필요 여부 확인
        DiagnosisDto.RedoCheckResponse response = DiagnosisDto.RedoCheckResponse.builder()
                .isRedoRequired(false)
                .message("재진단이 필요하지 않습니다.")
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "진단 통계 조회", description = "사용자의 진단 통계 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/stats")
    public ResponseEntity<DiagnosisDto.DiagnosisStatsResponse> getDiagnosisStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("사용자 {} 진단 통계 조회", userPrincipal.getId());
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        DiagnosisDto.DiagnosisStatsResponse response = diagnosisService.getDiagnosisStats(user);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 진단 상세 조회", description = "특정 진단 결과의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "진단 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "진단을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{diagnosisId}")
    public ResponseEntity<DiagnosisDto.DiagnosisResponse> getDiagnosisDetail(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "진단 ID") @PathVariable Long diagnosisId) {
        
        log.info("사용자 {} 진단 {} 상세 조회", userPrincipal.getId(), diagnosisId);
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        DiagnosisDto.DiagnosisResponse response = diagnosisService.getDiagnosisDetails(user, diagnosisId);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "긴급 상황 진단 조회", description = "심각한 수준의 진단 결과들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "긴급 상황 진단 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/urgent")
    public ResponseEntity<List<DiagnosisDto.DiagnosisHistoryItem>> getUrgentDiagnoses(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("사용자 {} 긴급 상황 진단 조회", userPrincipal.getId());
        
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // 긴급 상황 진단 조회 로직 구현 필요
        
        return ResponseEntity.ok(List.of());
    }
}