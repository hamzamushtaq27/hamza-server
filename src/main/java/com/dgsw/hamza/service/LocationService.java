package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.HospitalDto;
import com.dgsw.hamza.entity.Hospital;
import com.dgsw.hamza.repository.HospitalRepository;
import com.dgsw.hamza.util.LocationCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocationService {

    private final HospitalRepository hospitalRepository;

    /**
     * 사용자 위치 기반 최적화된 병원 추천
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> recommendOptimalHospitals(
            Double userLatitude, Double userLongitude, String severity) {
        
        log.info("사용자 위치 기반 최적화된 병원 추천 - 위도: {}, 경도: {}, 심각도: {}", 
                userLatitude, userLongitude, severity);

        // 심각도에 따른 검색 반경 결정
        double searchRadius = determineSearchRadius(severity);
        
        // 심각도에 따른 병원 우선순위 결정
        List<Object[]> nearbyHospitals = hospitalRepository.findNearbyHospitals(
                BigDecimal.valueOf(userLatitude),
                BigDecimal.valueOf(userLongitude),
                searchRadius
        );

        return nearbyHospitals.stream()
                .map(data -> {
                    Hospital hospital = (Hospital) data[0];
                    Double distance = (Double) data[1];
                    
                    // 심각도에 따른 우선순위 점수 계산
                    double priorityScore = calculatePriorityScore(hospital, distance, severity);
                    
                    HospitalDto.HospitalInfo hospitalInfo = convertToHospitalInfo(hospital, distance);
                    
                    return hospitalInfo;
                })
                .sorted((h1, h2) -> {
                    // 우선순위 점수 기반 정렬 (높은 점수 우선)
                    double score1 = calculateDisplayScore(h1, severity);
                    double score2 = calculateDisplayScore(h2, severity);
                    return Double.compare(score2, score1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 응급상황 대응 병원 추천
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> recommendEmergencyHospitals(
            Double userLatitude, Double userLongitude) {
        
        log.info("응급상황 대응 병원 추천 - 위도: {}, 경도: {}", userLatitude, userLongitude);

        // 응급상황에서는 더 넓은 반경으로 검색
        List<Object[]> emergencyHospitals = hospitalRepository.findNearbyEmergencyHospitals(
                BigDecimal.valueOf(userLatitude),
                BigDecimal.valueOf(userLongitude),
                20.0 // 20km 반경
        );

        return emergencyHospitals.stream()
                .map(data -> {
                    Hospital hospital = (Hospital) data[0];
                    Double distance = (Double) data[1];
                    return convertToHospitalInfo(hospital, distance);
                })
                .sorted((h1, h2) -> {
                    // 응급상황에서는 거리 우선 정렬
                    if (h1.getDistance() == null && h2.getDistance() == null) return 0;
                    if (h1.getDistance() == null) return 1;
                    if (h2.getDistance() == null) return -1;
                    return Double.compare(h1.getDistance(), h2.getDistance());
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 지역별 병원 밀도 분석
     */
    @Transactional(readOnly = true)
    public Object analyzeHospitalDensity(Double centerLat, Double centerLon, Double radiusKm) {
        log.info("지역별 병원 밀도 분석 - 중심좌표: ({}, {}), 반경: {}km", 
                centerLat, centerLon, radiusKm);

        // 경계 박스 계산
        LocationCalculator.BoundingBox boundingBox = LocationCalculator.calculateBoundingBox(
                centerLat, centerLon, radiusKm);

        // 경계 내 병원 조회
        List<Hospital> hospitalsInArea = hospitalRepository.findHospitalsInBounds(
                BigDecimal.valueOf(boundingBox.south),
                BigDecimal.valueOf(boundingBox.north),
                BigDecimal.valueOf(boundingBox.west),
                BigDecimal.valueOf(boundingBox.east)
        );

        // 밀도 계산
        double areaKm2 = Math.PI * radiusKm * radiusKm;
        double density = hospitalsInArea.size() / areaKm2;

        // 부서별 분포 계산
        var departmentDistribution = hospitalsInArea.stream()
                .collect(Collectors.groupingBy(
                        Hospital::getDepartment,
                        Collectors.counting()
                ));

        return new Object() {
            public final int totalHospitals = hospitalsInArea.size();
            public final double densityPerKm2 = density;
            public final double areaKm2 = LocationService.this.calculateAreaKm2(radiusKm);
            public final Object departmentDist = departmentDistribution; // 이름 변경
            public final double averageRating = hospitalsInArea.stream()
                    .filter(h -> h.getRating() != null)
                    .mapToDouble(h -> h.getRating().doubleValue())
                    .average()
                    .orElse(0.0);
        };
    }

    /**
     * 최적 경로 계산 (여러 병원 방문)
     */
    public List<HospitalDto.HospitalInfo> calculateOptimalRoute(
            Double startLat, Double startLon, List<Long> hospitalIds) {
        
        log.info("최적 경로 계산 - 시작점: ({}, {}), 병원 수: {}", 
                startLat, startLon, hospitalIds.size());

        // 병원 정보 조회
        List<Hospital> hospitals = hospitalRepository.findAllById(hospitalIds);
        
        // 각 병원까지의 거리 계산
        List<HospitalWithDistance> hospitalDistances = hospitals.stream()
                .map(hospital -> {
                    double distance = LocationCalculator.calculateDistance(
                            startLat, startLon,
                            hospital.getLatitude().doubleValue(),
                            hospital.getLongitude().doubleValue()
                    );
                    return new HospitalWithDistance(hospital, distance);
                })
                .collect(Collectors.toList());

        // 최적 경로 계산 (가장 가까운 순서로 정렬)
        hospitalDistances.sort((h1, h2) -> Double.compare(h1.distance, h2.distance));

        return hospitalDistances.stream()
                .map(hwd -> convertToHospitalInfo(hwd.hospital, hwd.distance))
                .collect(Collectors.toList());
    }

    /**
     * 병원 접근성 점수 계산
     */
    public double calculateAccessibilityScore(Hospital hospital, Double userLat, Double userLon) {
        double distance = LocationCalculator.calculateDistance(
                userLat, userLon,
                hospital.getLatitude().doubleValue(),
                hospital.getLongitude().doubleValue()
        );

        // 접근성 점수 계산 (거리, 평점, 편의시설 고려)
        double distanceScore = Math.max(0, 100 - (distance * 10)); // 거리 점수
        double ratingScore = hospital.getRating() != null ? 
                hospital.getRating().doubleValue() * 20 : 50; // 평점 점수
        double facilityScore = calculateFacilityScore(hospital); // 편의시설 점수

        return (distanceScore * 0.4) + (ratingScore * 0.4) + (facilityScore * 0.2);
    }

    /**
     * 지역별 병원 추천 순위
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getRegionalHospitalRanking(String region) {
        log.info("지역별 병원 추천 순위 - 지역: {}", region);

        List<Hospital> regionalHospitals = hospitalRepository.findByRegion(region);

        return regionalHospitals.stream()
                .map(hospital -> {
                    double score = calculateRegionalScore(hospital);
                    return convertToHospitalInfo(hospital, null);
                })
                .sorted((h1, h2) -> {
                    // 평점 기준 정렬
                    if (h1.getRating() == null && h2.getRating() == null) return 0;
                    if (h1.getRating() == null) return 1;
                    if (h2.getRating() == null) return -1;
                    return h2.getRating().compareTo(h1.getRating());
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private double determineSearchRadius(String severity) {
        switch (severity.toUpperCase()) {
            case "VERY_SEVERE":
            case "SEVERE":
                return 5.0; // 심각한 경우 가까운 병원 우선
            case "MODERATE":
                return 10.0; // 중등도는 적당한 거리
            case "MILD":
            case "NORMAL":
                return 20.0; // 경미한 경우 더 넓은 선택권
            default:
                return 10.0;
        }
    }

    private double calculatePriorityScore(Hospital hospital, Double distance, String severity) {
        double baseScore = 50.0;
        
        // 평점 점수 (0-30점)
        if (hospital.getRating() != null) {
            baseScore += hospital.getRating().doubleValue() * 6;
        }
        
        // 거리 점수 (0-20점)
        if (distance != null) {
            baseScore += Math.max(0, 20 - (distance * 2));
        }
        
        // 심각도별 보너스
        if ("VERY_SEVERE".equals(severity) || "SEVERE".equals(severity)) {
            if (Boolean.TRUE.equals(hospital.getIsEmergency())) {
                baseScore += 20; // 응급실 보너스
            }
        }
        
        // 편의시설 점수 (0-10점)
        baseScore += calculateFacilityScore(hospital);
        
        return baseScore;
    }

    private double calculateDisplayScore(HospitalDto.HospitalInfo hospital, String severity) {
        double score = 0.0;
        
        if (hospital.getRating() != null) {
            score += hospital.getRating().doubleValue() * 20;
        }
        
        if (hospital.getDistance() != null) {
            score += Math.max(0, 20 - (hospital.getDistance() * 2));
        }
        
        return score;
    }

    private double calculateFacilityScore(Hospital hospital) {
        double score = 0.0;
        
        if (Boolean.TRUE.equals(hospital.getParkingAvailable())) {
            score += 3.0;
        }
        
        if (Boolean.TRUE.equals(hospital.getWheelchairAccessible())) {
            score += 3.0;
        }
        
        if (Boolean.TRUE.equals(hospital.getIsEmergency())) {
            score += 4.0;
        }
        
        return score;
    }

    private double calculateRegionalScore(Hospital hospital) {
        double score = 0.0;
        
        if (hospital.getRating() != null) {
            score += hospital.getRating().doubleValue() * 20;
        }
        
        score += calculateFacilityScore(hospital);
        
        return score;
    }

    private HospitalDto.HospitalInfo convertToHospitalInfo(Hospital hospital, Double distance) {
        return HospitalDto.HospitalInfo.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .phone(hospital.getPhone())
                .website(hospital.getWebsite())
                .description(hospital.getDescription())
                .latitude(hospital.getLatitude())
                .longitude(hospital.getLongitude())
                .department(hospital.getDepartment())
                .rating(hospital.getRating())
                .operatingHours(hospital.getOperatingHours())
                .openTime(hospital.getOpenTime())
                .closeTime(hospital.getCloseTime())
                .lunchStart(hospital.getLunchStart())
                .lunchEnd(hospital.getLunchEnd())
                .isEmergency(hospital.getIsEmergency())
                .isActive(hospital.getIsActive())
                .parkingAvailable(hospital.getParkingAvailable())
                .wheelchairAccessible(hospital.getWheelchairAccessible())
                .specializedTreatments(hospital.getSpecializedTreatments())
                .distance(distance)
                .isOpen(hospital.isCurrentlyOpen())
                .build();
    }

    private double calculateAreaKm2(Double radiusKm) {
        return Math.PI * radiusKm * radiusKm;
    }

    // Inner class for hospital with distance
    private static class HospitalWithDistance {
        final Hospital hospital;
        final double distance;

        HospitalWithDistance(Hospital hospital, double distance) {
            this.hospital = hospital;
            this.distance = distance;
        }
    }
}