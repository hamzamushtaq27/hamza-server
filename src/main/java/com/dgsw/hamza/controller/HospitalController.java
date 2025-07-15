package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.HospitalDto;
import com.dgsw.hamza.service.HospitalService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hospital", description = "병원 검색 및 추천 API")
public class HospitalController {

    private final HospitalService hospitalService;

    @Operation(summary = "위치 기반 병원 검색", description = "사용자 위치를 기반으로 주변 병원을 검색합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 검색 성공",
                    content = @Content(schema = @Schema(implementation = HospitalDto.HospitalSearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 좌표 등)"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/search/nearby")
    public ResponseEntity<HospitalDto.HospitalSearchResponse> searchNearbyHospitals(
            @Valid @RequestBody HospitalDto.HospitalSearchRequest request) {

        log.info("위치 기반 병원 검색 요청 - 위도: {}, 경도: {}", request.getLatitude(), request.getLongitude());

        HospitalDto.HospitalSearchResponse response = hospitalService.searchNearbyHospitals(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "병원 상세 정보 조회", description = "특정 병원의 상세 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "병원을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/detail")
    public ResponseEntity<HospitalDto.HospitalDetailResponse> getHospitalDetail(
            @Valid @RequestBody HospitalDto.HospitalDetailRequest request) {

        log.info("병원 상세 정보 조회 - 병원 ID: {}", request.getHospitalId());

        HospitalDto.HospitalDetailResponse response = hospitalService.getHospitalDetail(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "키워드 기반 병원 검색", description = "병원 이름, 주소, 부서를 기반으로 병원을 검색합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색 키워드가 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> searchHospitalsByKeyword(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {

        log.info("키워드 기반 병원 검색 - 키워드: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.searchHospitalsByKeyword(keyword);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "부서별 병원 조회", description = "특정 부서의 병원들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부서별 병원 조회 성공"),
            @ApiResponse(responseCode = "400", description = "부서명이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/department/{department}")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getHospitalsByDepartment(
            @Parameter(description = "부서명") @PathVariable String department) {

        log.info("부서별 병원 조회 - 부서: {}", department);

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.getHospitalsByDepartment(department);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "응급실 병원 조회", description = "응급실이 있는 병원들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "응급실 병원 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/emergency")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getEmergencyHospitals() {
        log.info("응급실 병원 조회 요청");

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.getEmergencyHospitals();
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "위치 기반 응급실 병원 검색", description = "사용자 위치 기반으로 가까운 응급실 병원을 검색합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "응급실 병원 검색 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 좌표"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/emergency/nearby")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> searchNearbyEmergencyHospitals(
            @Parameter(description = "위도") @RequestParam Double latitude,
            @Parameter(description = "경도") @RequestParam Double longitude,
            @Parameter(description = "검색 반경 (km)") @RequestParam(defaultValue = "5.0") Double radiusKm) {

        log.info("위치 기반 응급실 병원 검색 - 위도: {}, 경도: {}, 반경: {}km", latitude, longitude, radiusKm);

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.searchNearbyEmergencyHospitals(
                latitude, longitude, radiusKm);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "높은 평점 병원 조회", description = "평점이 높은 병원들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "높은 평점 병원 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/top-rated")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getTopRatedHospitals(
            @Parameter(description = "조회할 병원 수") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("높은 평점 병원 조회 - limit: {}", limit);

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.getTopRatedHospitals(limit);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "병원 목록 조회", description = "필터 조건에 따라 병원 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/list")
    public ResponseEntity<HospitalDto.HospitalSearchResponse> getHospitalList(
            @Parameter(description = "부서명") @RequestParam(required = false) String department,
            @Parameter(description = "최소 평점") @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "응급실 여부") @RequestParam(required = false) Boolean emergency,
            @Parameter(description = "현재 운영 중인 병원만") @RequestParam(required = false) Boolean openNow,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") Integer size) {

        log.info("병원 목록 조회 - 부서: {}, 최소평점: {}, 응급실: {}, 운영중: {}", 
                department, minRating, emergency, openNow);

        HospitalDto.HospitalSearchResponse response = hospitalService.getHospitalList(
                department, minRating, emergency, openNow, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지역별 병원 조회", description = "특정 지역의 병원들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지역별 병원 조회 성공"),
            @ApiResponse(responseCode = "400", description = "지역명이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/region/{region}")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getHospitalsByRegion(
            @Parameter(description = "지역명") @PathVariable String region) {

        log.info("지역별 병원 조회 - 지역: {}", region);

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.getHospitalsByRegion(region);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "전문 분야별 병원 조회", description = "특정 전문 분야의 병원들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전문 분야별 병원 조회 성공"),
            @ApiResponse(responseCode = "400", description = "전문 분야명이 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<HospitalDto.HospitalInfo>> getHospitalsBySpecialty(
            @Parameter(description = "전문 분야명") @PathVariable String specialty) {

        log.info("전문 분야별 병원 조회 - 분야: {}", specialty);

        List<HospitalDto.HospitalInfo> hospitals = hospitalService.getHospitalsBySpecialty(specialty);
        return ResponseEntity.ok(hospitals);
    }

    @Operation(summary = "병원 통계 조회", description = "병원 관련 통계 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 통계 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/statistics")
    public ResponseEntity<Object> getHospitalStatistics() {
        log.info("병원 통계 조회 요청");

        Object statistics = hospitalService.getHospitalStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "주소 기반 좌표 변환", description = "주소를 위도/경도 좌표로 변환합니다 (지오코딩)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좌표 변환 성공"),
            @ApiResponse(responseCode = "400", description = "주소가 필요합니다"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/geocode")
    public ResponseEntity<HospitalDto.GeocodeResponse> geocodeAddress(
            @Valid @RequestBody HospitalDto.GeocodeRequest request) {

        log.info("주소 기반 좌표 변환 요청 - 주소: {}", request.getAddress());

        HospitalDto.GeocodeResponse response = hospitalService.geocodeAddress(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용 가능한 병원 부서 목록", description = "시스템에서 사용 가능한 병원 부서 목록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부서 목록 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAvailableDepartments() {
        log.info("사용 가능한 병원 부서 목록 조회");

        List<String> departments = List.of(
                "정신건강의학과",
                "신경과",
                "내과",
                "가정의학과",
                "소아청소년과",
                "재활의학과"
        );

        return ResponseEntity.ok(departments);
    }

    @Operation(summary = "병원 검색 팁", description = "효과적인 병원 검색을 위한 팁을 제공합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 팁 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/search-tips")
    public ResponseEntity<List<String>> getSearchTips() {
        log.info("병원 검색 팁 조회");

        List<String> tips = List.of(
                "위치 정보를 허용하면 더 정확한 거리 계산이 가능합니다",
                "응급상황이 아닌 경우 평점이 높은 병원을 선택해보세요",
                "진료과를 정확히 선택하면 더 적합한 병원을 찾을 수 있습니다",
                "운영시간을 확인하여 방문 가능한 시간대를 확인하세요",
                "주차장이나 휠체어 접근성 등 편의시설을 고려해보세요"
        );

        return ResponseEntity.ok(tips);
    }
}