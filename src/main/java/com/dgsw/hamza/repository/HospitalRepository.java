package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    /**
     * 활성화된 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.name")
    List<Hospital> findActiveHospitals();

    /**
     * 특정 부서의 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.department = :department AND h.isActive = true ORDER BY h.name")
    List<Hospital> findByDepartmentAndActive(@Param("department") String department);

    /**
     * 평점 이상의 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.rating >= :minRating AND h.isActive = true ORDER BY h.rating DESC")
    List<Hospital> findByRatingGreaterThanEqual(@Param("minRating") BigDecimal minRating);

    /**
     * 병원 이름으로 검색
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
           "LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY h.name")
    List<Hospital> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 복합 검색 (이름, 주소, 부서)
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
           "(LOWER(h.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.department) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY h.name")
    List<Hospital> searchHospitals(@Param("keyword") String keyword);

    /**
     * 위치 기반 병원 검색 (반경 내)
     * Haversine 공식을 사용한 거리 계산
     */
    @Query("SELECT h, " +
           "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) AS distance " +
           "FROM Hospital h " +
           "WHERE h.isActive = true " +
           "AND (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) <= :radiusKm " +
           "ORDER BY distance ASC")
    List<Object[]> findNearbyHospitals(@Param("latitude") BigDecimal latitude,
                                       @Param("longitude") BigDecimal longitude,
                                       @Param("radiusKm") Double radiusKm);

    /**
     * 위치 기반 병원 검색 (부서 필터 포함)
     */
    @Query("SELECT h, " +
           "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) AS distance " +
           "FROM Hospital h " +
           "WHERE h.isActive = true " +
           "AND h.department = :department " +
           "AND (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) <= :radiusKm " +
           "ORDER BY distance ASC")
    List<Object[]> findNearbyHospitalsByDepartment(@Param("latitude") BigDecimal latitude,
                                                   @Param("longitude") BigDecimal longitude,
                                                   @Param("radiusKm") Double radiusKm,
                                                   @Param("department") String department);

    /**
     * 위치 기반 병원 검색 (평점 필터 포함)
     */
    @Query("SELECT h, " +
           "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) AS distance " +
           "FROM Hospital h " +
           "WHERE h.isActive = true " +
           "AND h.rating >= :minRating " +
           "AND (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) <= :radiusKm " +
           "ORDER BY distance ASC")
    List<Object[]> findNearbyHospitalsByRating(@Param("latitude") BigDecimal latitude,
                                               @Param("longitude") BigDecimal longitude,
                                               @Param("radiusKm") Double radiusKm,
                                               @Param("minRating") BigDecimal minRating);

    /**
     * 응급실 있는 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isEmergency = true AND h.isActive = true ORDER BY h.name")
    List<Hospital> findEmergencyHospitals();

    /**
     * 위치 기반 응급실 병원 검색
     */
    @Query("SELECT h, " +
           "(6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) AS distance " +
           "FROM Hospital h " +
           "WHERE h.isActive = true AND h.isEmergency = true " +
           "AND (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) <= :radiusKm " +
           "ORDER BY distance ASC")
    List<Object[]> findNearbyEmergencyHospitals(@Param("latitude") BigDecimal latitude,
                                                @Param("longitude") BigDecimal longitude,
                                                @Param("radiusKm") Double radiusKm);

    /**
     * 편의시설 필터 (주차장, 휠체어 접근)
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true " +
           "AND (:parkingRequired = false OR h.parkingAvailable = true) " +
           "AND (:wheelchairRequired = false OR h.wheelchairAccessible = true) " +
           "ORDER BY h.name")
    List<Hospital> findByFacilities(@Param("parkingRequired") Boolean parkingRequired,
                                    @Param("wheelchairRequired") Boolean wheelchairRequired);

    /**
     * 현재 운영 중인 병원 조회
     */
    // DB에서 직접 isOpen 필터링 불가, 전체 조회 후 서비스에서 isCurrentlyOpen()으로 필터링 필요
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.name")
    List<Hospital> findAllActiveHospitalsForOpenFilter();

    /**
     * 병원 통계 - 부서별 분포
     */
    @Query("SELECT h.department, COUNT(h) FROM Hospital h WHERE h.isActive = true GROUP BY h.department")
    List<Object[]> findDepartmentDistribution();

    /**
     * 병원 통계 - 평점별 분포
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN h.rating >= 4.5 THEN '매우 좋음' " +
           "WHEN h.rating >= 4.0 THEN '좋음' " +
           "WHEN h.rating >= 3.0 THEN '보통' " +
           "ELSE '개선 필요' " +
           "END as ratingGroup, " +
           "COUNT(h) " +
           "FROM Hospital h WHERE h.isActive = true " +
           "GROUP BY ratingGroup " +
           "ORDER BY MIN(h.rating) DESC")
    List<Object[]> findRatingDistribution();

    /**
     * 평균 평점 조회
     */
    @Query("SELECT AVG(h.rating) FROM Hospital h WHERE h.isActive = true")
    Double findAverageRating();

    /**
     * 최고 평점 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.rating DESC LIMIT :limit")
    List<Hospital> findTopRatedHospitals(@Param("limit") Integer limit);

    /**
     * 특정 지역의 병원 조회 (주소 기반)
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
           "LOWER(h.address) LIKE LOWER(CONCAT('%', :region, '%')) ORDER BY h.name")
    List<Hospital> findByRegion(@Param("region") String region);

    /**
     * 전문 치료 분야별 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
           "LOWER(h.specializedTreatments) LIKE LOWER(CONCAT('%', :treatment, '%')) ORDER BY h.name")
    List<Hospital> findBySpecializedTreatment(@Param("treatment") String treatment);

    /**
     * 병원 개수 조회
     */
    @Query("SELECT COUNT(h) FROM Hospital h WHERE h.isActive = true")
    Long countActiveHospitals();

    /**
     * 부서별 병원 개수
     */
    @Query("SELECT COUNT(h) FROM Hospital h WHERE h.department = :department AND h.isActive = true")
    Long countByDepartment(@Param("department") String department);

    /**
     * 복합 필터 검색 (페이징)
     */
    // DB에서 직접 isOpen 필터링 불가, 전체 조회 후 서비스에서 isCurrentlyOpen()으로 필터링 필요
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true " +
           "AND (:department IS NULL OR h.department = :department) " +
           "AND (:minRating IS NULL OR h.rating >= :minRating) " +
           "AND (:emergency IS NULL OR h.isEmergency = :emergency) " +
           "ORDER BY h.rating DESC, h.name ASC")
    Page<Hospital> findHospitalsWithFilters(@Param("department") String department,
                                            @Param("minRating") BigDecimal minRating,
                                            @Param("emergency") Boolean emergency,
                                            Pageable pageable);

    /**
     * 병원 이름 중복 체크
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
           "FROM Hospital h WHERE h.name = :name AND h.address = :address AND h.id != :excludeId")
    Boolean existsDuplicateHospital(@Param("name") String name,
                                    @Param("address") String address,
                                    @Param("excludeId") Long excludeId);

    /**
     * 가장 가까운 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true " +
           "ORDER BY (6371 * ACOS(COS(RADIANS(:latitude)) * COS(RADIANS(h.latitude)) * " +
           "COS(RADIANS(h.longitude) - RADIANS(:longitude)) + " +
           "SIN(RADIANS(:latitude)) * SIN(RADIANS(h.latitude)))) ASC " +
           "LIMIT 1")
    Optional<Hospital> findNearestHospital(@Param("latitude") BigDecimal latitude,
                                           @Param("longitude") BigDecimal longitude);

    /**
     * 좌표 범위 내 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true " +
           "AND h.latitude BETWEEN :minLat AND :maxLat " +
           "AND h.longitude BETWEEN :minLon AND :maxLon " +
           "ORDER BY h.name")
    List<Hospital> findHospitalsInBounds(@Param("minLat") BigDecimal minLat,
                                         @Param("maxLat") BigDecimal maxLat,
                                         @Param("minLon") BigDecimal minLon,
                                         @Param("maxLon") BigDecimal maxLon);

    /**
     * 최근 추가된 병원 조회
     */
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.createdAt DESC LIMIT :limit")
    List<Hospital> findRecentHospitals(@Param("limit") Integer limit);
}