package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.Treatment;
import com.dgsw.hamza.enums.TreatmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    /**
     * 활성화된 치료법 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true ORDER BY t.name")
    List<Treatment> findActiveTreatments();

    /**
     * 치료법 유형별 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.type = :type AND t.isActive = true ORDER BY t.name")
    List<Treatment> findByTypeAndActive(@Param("type") TreatmentType type);

    /**
     * 난이도별 치료법 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.difficultyLevel = :level AND t.isActive = true ORDER BY t.name")
    List<Treatment> findByDifficultyLevelAndActive(@Param("level") String level);

    /**
     * 소요시간 범위별 치료법 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.durationMinutes <= :maxDuration AND t.isActive = true ORDER BY t.durationMinutes")
    List<Treatment> findByDurationLessThanEqual(@Param("maxDuration") Integer maxDuration);

    /**
     * 치료법 검색 (이름 또는 설명)
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.name")
    List<Treatment> searchTreatments(@Param("keyword") String keyword);

    /**
     * 복합 조건 검색
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:level IS NULL OR t.difficultyLevel = :level) " +
           "AND (:maxDuration IS NULL OR t.durationMinutes <= :maxDuration) " +
           "ORDER BY t.name")
    Page<Treatment> findTreatmentsWithFilters(@Param("type") TreatmentType type,
                                              @Param("level") String level,
                                              @Param("maxDuration") Integer maxDuration,
                                              Pageable pageable);

    /**
     * 치료법 유형별 개수 조회
     */
    @Query("SELECT t.type, COUNT(t) FROM Treatment t WHERE t.isActive = true GROUP BY t.type")
    List<Object[]> countByType();

    /**
     * 활성화된 치료법 개수
     */
    @Query("SELECT COUNT(t) FROM Treatment t WHERE t.isActive = true")
    Long countActiveTreatments();

    /**
     * 평균 소요시간 조회
     */
    @Query("SELECT AVG(t.durationMinutes) FROM Treatment t WHERE t.isActive = true")
    Double findAverageDuration();

    /**
     * 특정 유형의 평균 소요시간
     */
    @Query("SELECT AVG(t.durationMinutes) FROM Treatment t WHERE t.type = :type AND t.isActive = true")
    Double findAverageDurationByType(@Param("type") TreatmentType type);

    /**
     * 가장 짧은 소요시간의 치료법
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true ORDER BY t.durationMinutes ASC LIMIT 1")
    Optional<Treatment> findShortestTreatment();

    /**
     * 가장 긴 소요시간의 치료법
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true ORDER BY t.durationMinutes DESC LIMIT 1")
    Optional<Treatment> findLongestTreatment();

    /**
     * 특정 유형의 치료법 중 난이도별 분포
     */
    @Query("SELECT t.difficultyLevel, COUNT(t) FROM Treatment t WHERE t.type = :type AND t.isActive = true GROUP BY t.difficultyLevel")
    List<Object[]> findDifficultyDistributionByType(@Param("type") TreatmentType type);

    /**
     * 소요시간 범위별 치료법 분포
     */
    @Query("SELECT " +
           "CASE " +
           "WHEN t.durationMinutes <= 15 THEN '15분 이하' " +
           "WHEN t.durationMinutes <= 30 THEN '16-30분' " +
           "WHEN t.durationMinutes <= 60 THEN '31-60분' " +
           "ELSE '60분 초과' " +
           "END as durationRange, " +
           "COUNT(t) " +
           "FROM Treatment t WHERE t.isActive = true " +
           "GROUP BY durationRange " +
           "ORDER BY MIN(t.durationMinutes)")
    List<Object[]> findDurationDistribution();

    /**
     * 추천 가능한 치료법 조회 (활성화되고 컨텐츠가 있는 것)
     */
    @Query("SELECT DISTINCT t FROM Treatment t " +
           "LEFT JOIN t.contents c " +
           "WHERE t.isActive = true " +
           "AND (c.isActive = true OR c IS NULL) " +
           "ORDER BY t.name")
    List<Treatment> findRecommendableTreatments();

    /**
     * 특정 치료법의 컨텐츠 포함 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM Treatment t LEFT JOIN t.contents c " +
           "WHERE t.id = :treatmentId AND c.isActive = true")
    Boolean hasActiveContents(@Param("treatmentId") Long treatmentId);

    /**
     * 사용자가 과거에 추천받은 치료법 조회
     */
    @Query("SELECT DISTINCT t FROM Treatment t " +
           "JOIN Recommendation r ON r.treatment = t " +
           "WHERE r.user.id = :userId " +
           "ORDER BY r.recommendedDate DESC")
    List<Treatment> findPreviouslyRecommendedTreatments(@Param("userId") Long userId);

    /**
     * 사용자가 과거에 추천받지 않은 치료법 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true " +
           "AND t.id NOT IN (" +
           "SELECT DISTINCT r.treatment.id FROM Recommendation r WHERE r.user.id = :userId" +
           ") ORDER BY t.name")
    List<Treatment> findNotRecommendedTreatments(@Param("userId") Long userId);

    /**
     * 인기 있는 치료법 조회 (추천 받은 횟수 기준)
     */
    @Query("SELECT t, COUNT(r) as recommendationCount " +
           "FROM Treatment t LEFT JOIN Recommendation r ON r.treatment = t " +
           "WHERE t.isActive = true " +
           "GROUP BY t " +
           "ORDER BY recommendationCount DESC")
    List<Object[]> findPopularTreatments();

    /**
     * 특정 치료법의 추천 통계
     */
    @Query("SELECT " +
           "COUNT(r) as totalRecommendations, " +
           "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as completedCount, " +
           "COUNT(CASE WHEN r.status = 'ACTIVE' THEN 1 END) as activeCount " +
           "FROM Recommendation r WHERE r.treatment.id = :treatmentId")
    Object[] findTreatmentStats(@Param("treatmentId") Long treatmentId);

    /**
     * 치료법 이름으로 검색
     */
    @Query("SELECT t FROM Treatment t WHERE t.name = :name AND t.isActive = true")
    Optional<Treatment> findByNameAndActive(@Param("name") String name);

    /**
     * 중복 치료법 검사
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM Treatment t WHERE t.name = :name AND t.type = :type AND t.id != :excludeId")
    Boolean existsDuplicateTreatment(@Param("name") String name,
                                     @Param("type") TreatmentType type,
                                     @Param("excludeId") Long excludeId);

    /**
     * 최근 생성된 치료법 조회
     */
    @Query("SELECT t FROM Treatment t WHERE t.isActive = true ORDER BY t.createdAt DESC LIMIT :limit")
    List<Treatment> findRecentTreatments(@Param("limit") Integer limit);
}