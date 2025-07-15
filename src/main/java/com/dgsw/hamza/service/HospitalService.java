package com.dgsw.hamza.service;

import com.dgsw.hamza.config.CacheConfig;
import com.dgsw.hamza.dto.HospitalDto;
import com.dgsw.hamza.entity.Hospital;
import com.dgsw.hamza.repository.HospitalRepository;
import com.dgsw.hamza.util.LocationCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    /**
     * 위치 기반 병원 검색
     */
    @Transactional(readOnly = true)
    public HospitalDto.HospitalSearchResponse searchNearbyHospitals(HospitalDto.HospitalSearchRequest request) {
        log.info("위치 기반 병원 검색 - 위도: {}, 경도: {}, 반경: {}m", 
                request.getLatitude(), request.getLongitude(), request.getRadius());

        // 위치 유효성 검증
        if (!LocationCalculator.isValidCoordinate(request.getLatitude(), request.getLongitude())) {
            throw new IllegalArgumentException("유효하지 않은 위치 정보입니다.");
        }

        // 반경을 km로 변환
        double radiusKm = request.getRadius() != null ? request.getRadius() / 1000.0 : 10.0;
        
        List<Object[]> nearbyData;
        
        // 부서별 필터링
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            nearbyData = hospitalRepository.findNearbyHospitalsByDepartment(
                    BigDecimal.valueOf(request.getLatitude()),
                    BigDecimal.valueOf(request.getLongitude()),
                    radiusKm,
                    request.getKeyword()
            );
        } else {
            nearbyData = hospitalRepository.findNearbyHospitals(
                    BigDecimal.valueOf(request.getLatitude()),
                    BigDecimal.valueOf(request.getLongitude()),
                    radiusKm
            );
        }

        // 응급실 필터링
        if (Boolean.TRUE.equals(request.getEmergencyOnly())) {
            nearbyData = hospitalRepository.findNearbyEmergencyHospitals(
                    BigDecimal.valueOf(request.getLatitude()),
                    BigDecimal.valueOf(request.getLongitude()),
                    radiusKm
            );
        }

        // 결과 변환
        List<HospitalDto.HospitalInfo> hospitals = nearbyData.stream()
                .limit(request.getMaxResults() != null ? request.getMaxResults() : 20)
                .map(data -> {
                    Hospital hospital = (Hospital) data[0];
                    Double distance = (Double) data[1];
                    
                    return convertToHospitalInfo(hospital, distance);
                })
                .collect(Collectors.toList());

        // 현재 운영 중인 병원만 필터링
        if (Boolean.TRUE.equals(request.getOpenNow())) {
            hospitals = hospitals.stream()
                    .filter(h -> Boolean.TRUE.equals(h.getIsOpen()))
                    .collect(Collectors.toList());
        }

        // 정렬
        hospitals = sortHospitals(hospitals, request.getSortBy());

        // 검색 위치 정보 생성
        HospitalDto.LocationDto searchLocation = HospitalDto.LocationDto.builder()
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        return HospitalDto.HospitalSearchResponse.builder()
                .hospitals(hospitals)
                .totalCount(hospitals.size())
                .searchKeyword(request.getKeyword())
                .searchLocation(searchLocation)
                .searchRadius(request.getRadius())
                .message(generateSearchMessage(hospitals.size(), radiusKm))
                .hasMore(nearbyData.size() > hospitals.size())
                .build();
    }

    /**
     * 병원 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public HospitalDto.HospitalDetailResponse getHospitalDetail(HospitalDto.HospitalDetailRequest request) {
        log.info("병원 상세 정보 조회 - 병원 ID: {}", request.getHospitalId());

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new IllegalArgumentException("병원을 찾을 수 없습니다: " + request.getHospitalId()));

        // 사용자 위치가 있으면 거리 계산
        Double distance = null;
        if (request.getUserLatitude() != null && request.getUserLongitude() != null) {
            distance = LocationCalculator.calculateDistance(
                    request.getUserLatitude(), request.getUserLongitude(),
                    hospital.getLatitude().doubleValue(), hospital.getLongitude().doubleValue()
            );
        }

        HospitalDto.HospitalInfo hospitalInfo = convertToHospitalInfo(hospital, distance);

        // 운영시간 정보 생성
        HospitalDto.OpeningHours openingHours = generateOpeningHours(hospital);

        // 연락처 정보 생성
        HospitalDto.ContactInfo contactInfo = HospitalDto.ContactInfo.builder()
                .phone(hospital.getPhone())
                .website(hospital.getWebsite())
                .googleUrl(LocationCalculator.generateGoogleMapsUrl(
                        hospital.getLatitude().doubleValue(),
                        hospital.getLongitude().doubleValue()
                ))
                .build();

        // 위치 정보 생성
        HospitalDto.LocationInfo locationInfo = HospitalDto.LocationInfo.builder()
                .fullAddress(hospital.getAddress())
                .latitude(hospital.getLatitude().doubleValue())
                .longitude(hospital.getLongitude().doubleValue())
                .directions(generateDirectionsUrl(request, hospital))
                .build();

        return HospitalDto.HospitalDetailResponse.builder()
                .hospital(hospitalInfo)
                .services(generateServices(hospital))
                .specialties(generateSpecialties(hospital))
                .openingHours(openingHours)
                .contactInfo(contactInfo)
                .locationInfo(locationInfo)
                .build();
    }

    /**
     * 병원 검색 (키워드 기반)
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> searchHospitalsByKeyword(String keyword) {
        log.info("병원 키워드 검색 - 키워드: {}", keyword);

        List<Hospital> hospitals = hospitalRepository.searchHospitals(keyword);

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 부서별 병원 조회
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getHospitalsByDepartment(String department) {
        log.info("부서별 병원 조회 - 부서: {}", department);

        List<Hospital> hospitals = hospitalRepository.findByDepartmentAndActive(department);

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 응급실 병원 조회
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getEmergencyHospitals() {
        log.info("응급실 병원 조회");

        List<Hospital> hospitals = hospitalRepository.findEmergencyHospitals();

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 위치 기반 응급실 병원 검색
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> searchNearbyEmergencyHospitals(
            Double latitude, Double longitude, Double radiusKm) {
        
        log.info("위치 기반 응급실 병원 검색 - 위도: {}, 경도: {}, 반경: {}km", 
                latitude, longitude, radiusKm);

        if (!LocationCalculator.isValidCoordinate(latitude, longitude)) {
            throw new IllegalArgumentException("유효하지 않은 위치 정보입니다.");
        }

        List<Object[]> nearbyData = hospitalRepository.findNearbyEmergencyHospitals(
                BigDecimal.valueOf(latitude),
                BigDecimal.valueOf(longitude),
                radiusKm
        );

        return nearbyData.stream()
                .map(data -> {
                    Hospital hospital = (Hospital) data[0];
                    Double distance = (Double) data[1];
                    return convertToHospitalInfo(hospital, distance);
                })
                .collect(Collectors.toList());
    }

    /**
     * 높은 평점 병원 조회
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getTopRatedHospitals(Integer limit) {
        log.info("높은 평점 병원 조회 - limit: {}", limit);

        List<Hospital> hospitals = hospitalRepository.findTopRatedHospitals(limit != null ? limit : 10);

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 병원 목록 조회 (필터링 및 페이징)
     */
    @Transactional(readOnly = true)
    public HospitalDto.HospitalSearchResponse getHospitalList(
            String department, BigDecimal minRating, Boolean emergency, Boolean openNow,
            Integer page, Integer size) {
        
        log.info("병원 목록 조회 - 부서: {}, 최소평점: {}, 응급실: {}, 운영중: {}", 
                department, minRating, emergency, openNow);

        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);

        Page<Hospital> hospitalPage = hospitalRepository.findHospitalsWithFilters(
                department, minRating, emergency, pageable);

        List<Hospital> filteredHospitals = hospitalPage.getContent();
        if (openNow != null) {
            filteredHospitals = filteredHospitals.stream()
                .filter(h -> h.isCurrentlyOpen() == openNow)
                .collect(Collectors.toList());
        }

        List<HospitalDto.HospitalInfo> hospitals = filteredHospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());

        return HospitalDto.HospitalSearchResponse.builder()
                .hospitals(hospitals)
                .totalCount(hospitals.size())
                .message(String.format("총 %d개의 병원을 찾았습니다.", hospitals.size()))
                .hasMore(hospitalPage.hasNext())
                .build();
    }

    /**
     * 지역별 병원 조회
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getHospitalsByRegion(String region) {
        log.info("지역별 병원 조회 - 지역: {}", region);

        List<Hospital> hospitals = hospitalRepository.findByRegion(region);

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 전문 치료 분야별 병원 조회
     */
    @Transactional(readOnly = true)
    public List<HospitalDto.HospitalInfo> getHospitalsBySpecialty(String specialty) {
        log.info("전문 치료 분야별 병원 조회 - 분야: {}", specialty);

        List<Hospital> hospitals = hospitalRepository.findBySpecializedTreatment(specialty);

        return hospitals.stream()
                .map(hospital -> convertToHospitalInfo(hospital, null))
                .collect(Collectors.toList());
    }

    /**
     * 병원 통계 조회
     */
    @Transactional(readOnly = true)
    public Object getHospitalStatistics() {
        log.info("병원 통계 조회");

        Long totalHospitals = hospitalRepository.countActiveHospitals();
        Double averageRating = hospitalRepository.findAverageRating();
        List<Object[]> departmentStats = hospitalRepository.findDepartmentDistribution();
        List<Object[]> ratingStats = hospitalRepository.findRatingDistribution();

        return new Object() {
            public final Long totalHospitals = HospitalService.this.getTotalHospitals();
            public final Double averageRating = HospitalService.this.getAverageRating();
            public final List<Object[]> departmentDistribution = departmentStats;
            public final List<Object[]> ratingDistribution = ratingStats;
        };
    }

    /**
     * 주소 기반 좌표 변환 (지오코딩)
     */
    public HospitalDto.GeocodeResponse geocodeAddress(HospitalDto.GeocodeRequest request) {
        log.info("주소 기반 좌표 변환 - 주소: {}", request.getAddress());

        // 실제 구현에서는 Google Maps API나 다른 지오코딩 서비스 사용
        // 여기서는 샘플 구현
        return HospitalDto.GeocodeResponse.builder()
                .latitude(37.5665)
                .longitude(126.9780)
                .formattedAddress("서울특별시 중구 을지로 1가")
                .status("OK")
                .message("주소 변환이 완료되었습니다.")
                .build();
    }

    // Private helper methods

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

    private List<HospitalDto.HospitalInfo> sortHospitals(List<HospitalDto.HospitalInfo> hospitals, String sortBy) {
        if (sortBy == null) {
            return hospitals;
        }

        switch (sortBy.toLowerCase()) {
            case "distance":
                return hospitals.stream()
                        .sorted((h1, h2) -> {
                            if (h1.getDistance() == null && h2.getDistance() == null) return 0;
                            if (h1.getDistance() == null) return 1;
                            if (h2.getDistance() == null) return -1;
                            return Double.compare(h1.getDistance(), h2.getDistance());
                        })
                        .collect(Collectors.toList());
            case "rating":
                return hospitals.stream()
                        .sorted((h1, h2) -> {
                            if (h1.getRating() == null && h2.getRating() == null) return 0;
                            if (h1.getRating() == null) return 1;
                            if (h2.getRating() == null) return -1;
                            return h2.getRating().compareTo(h1.getRating());
                        })
                        .collect(Collectors.toList());
            case "name":
                return hospitals.stream()
                        .sorted((h1, h2) -> h1.getName().compareTo(h2.getName()))
                        .collect(Collectors.toList());
            default:
                return hospitals;
        }
    }

    private String generateSearchMessage(int count, double radiusKm) {
        if (count == 0) {
            return String.format("반경 %.1fkm 내에서 병원을 찾을 수 없습니다.", radiusKm);
        } else {
            return String.format("반경 %.1fkm 내에서 %d개의 병원을 찾았습니다.", radiusKm, count);
        }
    }

    private HospitalDto.OpeningHours generateOpeningHours(Hospital hospital) {
        // 실제 구현에서는 병원별 운영시간 정보 활용
        return HospitalDto.OpeningHours.builder()
                .openNow(hospital.isCurrentlyOpen())
                .weekdayText(List.of(
                        "월요일: 09:00 - 18:00",
                        "화요일: 09:00 - 18:00",
                        "수요일: 09:00 - 18:00",
                        "목요일: 09:00 - 18:00",
                        "금요일: 09:00 - 18:00",
                        "토요일: 09:00 - 13:00",
                        "일요일: 휴무"
                ))
                .build();
    }

    private List<String> generateServices(Hospital hospital) {
        // 실제 구현에서는 병원별 서비스 정보 활용
        return List.of(
                "정신건강의학과 진료",
                "심리상담",
                "약물치료",
                "집단치료",
                "응급처치"
        );
    }

    private List<String> generateSpecialties(Hospital hospital) {
        // 실제 구현에서는 병원별 전문 분야 정보 활용
        if (hospital.getSpecializedTreatments() != null) {
            return List.of(hospital.getSpecializedTreatments().split(","));
        }
        return List.of("우울증", "불안장애", "조울증", "정신분열증");
    }

    private String generateDirectionsUrl(HospitalDto.HospitalDetailRequest request, Hospital hospital) {
        if (request.getUserLatitude() != null && request.getUserLongitude() != null) {
            return LocationCalculator.generateDirectionsUrl(
                    request.getUserLatitude(), request.getUserLongitude(),
                    hospital.getLatitude().doubleValue(), hospital.getLongitude().doubleValue()
            );
        }
        return LocationCalculator.generateGoogleMapsUrl(
                hospital.getLatitude().doubleValue(),
                hospital.getLongitude().doubleValue()
        );
    }

    private Long getTotalHospitals() {
        return hospitalRepository.countActiveHospitals();
    }

    private Double getAverageRating() {
        return hospitalRepository.findAverageRating();
    }
}