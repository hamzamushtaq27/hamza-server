package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.HospitalDto;
import com.dgsw.hamza.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Location", description = "위치 기반 서비스 API")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "최적화된 병원 추천", description = "사용자 위치와 심각도를 기반으로 최적의 병원을 추천합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 추천 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 위치 정보"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/hospitals/optimal")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> recommendOptimalHospitals(
            @Parameter(description = "사용자 위도") @RequestParam Double latitude,
            @Parameter(description = "사용자 경도") @RequestParam Double longitude,
            @Parameter(description = "심각도 (NORMAL, MILD, MODERATE, SEVERE, VERY_SEVERE)") 
            @RequestParam(defaultValue = "MODERATE") String severity) {

        log.info("최적화된 병원 추천 요청 - 위도: {}, 경도: {}, 심각도: {}", latitude, longitude, severity);

        List<HospitalDto.HospitalInfo> hospitals = locationService.recommendOptimalHospitals(
                latitude, longitude, severity);

        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "응급상황 병원 추천", description = "응급상황에 대응 가능한 가까운 병원을 추천합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "응급 병원 추천 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 위치 정보"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/hospitals/emergency")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> recommendEmergencyHospitals(
            @Parameter(description = "사용자 위도") @RequestParam Double latitude,
            @Parameter(description = "사용자 경도") @RequestParam Double longitude) {

        log.info("응급상황 병원 추천 요청 - 위도: {}, 경도: {}", latitude, longitude);

        List<HospitalDto.HospitalInfo> hospitals = locationService.recommendEmergencyHospitals(
                latitude, longitude);

        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "지역별 병원 밀도 분석", description = "특정 지역의 병원 밀도를 분석합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "밀도 분석 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 위치 정보"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/analysis/density")
    public ResponseEntity<Object> analyzeHospitalDensity(
            @Parameter(description = "중심점 위도") @RequestParam Double centerLat,
            @Parameter(description = "중심점 경도") @RequestParam Double centerLon,
            @Parameter(description = "분석 반경 (km)") @RequestParam(defaultValue = "10.0") Double radiusKm) {

        log.info("지역별 병원 밀도 분석 요청 - 중심좌표: ({}, {}), 반경: {}km", 
                centerLat, centerLon, radiusKm);

        Object analysis = locationService.analyzeHospitalDensity(centerLat, centerLon, radiusKm);

        return ResponseEntity.ok(analysis);
    }

    @Operation(summary = "최적 경로 계산", description = "여러 병원을 방문하는 최적 경로를 계산합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경로 계산 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/route/optimal")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> calculateOptimalRoute(
            @Parameter(description = "시작점 위도") @RequestParam Double startLat,
            @Parameter(description = "시작점 경도") @RequestParam Double startLon,
            @Parameter(description = "방문할 병원 ID 목록") @RequestBody List<Long> hospitalIds) {

        log.info("최적 경로 계산 요청 - 시작점: ({}, {}), 병원 수: {}", 
                startLat, startLon, hospitalIds.size());

        if (hospitalIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<HospitalDto.HospitalInfo> route = locationService.calculateOptimalRoute(
                startLat, startLon, hospitalIds);

        return ResponseEntity.ok(route);
    }

    @Operation(summary = "지역별 병원 순위", description = "특정 지역의 병원 추천 순위를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지역별 순위 조회 성공"),
            @ApiResponse(responseCode = "400", description = "지역명이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/hospitals/regional-ranking")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getRegionalHospitalRanking(
            @Parameter(description = "지역명") @RequestParam String region) {

        log.info("지역별 병원 순위 요청 - 지역: {}", region);

        if (region == null || region.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<HospitalDto.HospitalInfo> hospitals = locationService.getRegionalHospitalRanking(region);

        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "위치 서비스 도움말", description = "위치 기반 서비스 사용법을 안내합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도움말 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/help")
    public ResponseEntity<Object> getLocationServiceHelp() {
        log.info("위치 서비스 도움말 요청");

        Object help = new Object() {
            public final String title = "위치 기반 병원 추천 서비스";
            public final List<String> features = List.of(
                    "사용자 위치 기반 가까운 병원 검색",
                    "심각도에 따른 맞춤형 병원 추천",
                    "응급상황 대응 병원 즉시 검색",
                    "지역별 병원 밀도 분석",
                    "여러 병원 방문 최적 경로 계산",
                    "접근성 점수 기반 병원 순위"
            );
            public final List<String> tips = List.of(
                    "정확한 위치 정보 제공 시 더 정확한 추천 가능",
                    "심각도를 정확히 입력하면 적절한 병원 추천",
                    "응급상황에서는 '/emergency' 엔드포인트 사용",
                    "평점과 거리를 종합적으로 고려한 추천",
                    "편의시설 정보도 함께 제공"
            );
            public final Object severityLevels = new Object() {
                public final String NORMAL = "정상 - 예방 및 건강관리";
                public final String MILD = "경미 - 가벼운 증상";
                public final String MODERATE = "중등도 - 적극적 치료 필요";
                public final String SEVERE = "심각 - 즉시 전문 치료";
                public final String VERY_SEVERE = "매우 심각 - 응급 처치 필요";
            };
        };

        return ResponseEntity.ok(help);
    }
}