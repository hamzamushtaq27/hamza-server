package com.dgsw.hamza.repository;

import com.dgsw.hamza.entity.Diagnosis;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    /**
     * 사용자의 가장 최근 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true ORDER BY d.diagnosisDate DESC LIMIT 1")
    Optional<Diagnosis> findLatestByUser(@Param("user") User user);

    /**
     * 사용자의 모든 진단 조회 (최신 순)
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findAllByUserOrderByDiagnosisDateDesc(@Param("user") User user);

    /**
     * 사용자의 진단 히스토리 (페이징)
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true ORDER BY d.diagnosisDate DESC")
    Page<Diagnosis> findByUserOrderByDiagnosisDateDesc(@Param("user") User user, Pageable pageable);

    /**
     * 특정 기간 내 사용자의 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.diagnosisDate BETWEEN :startDate AND :endDate ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findByUserAndDiagnosisDateBetween(@Param("user") User user,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 진단 개수 조회
     */
    @Query("SELECT COUNT(d) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true")
    Long countByUser(@Param("user") User user);

    /**
     * 특정 심각도 이상의 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.severity IN :severities ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findByUserAndSeverityIn(@Param("user") User user,
                                            @Param("severities") List<DiagnosisSeverity> severities);

    /**
     * 사용자의 평균 점수 조회
     */
    @Query("SELECT AVG(d.totalScore) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true")
    Double findAverageScoreByUser(@Param("user") User user);

    /**
     * 사용자의 최고/최저 점수 조회
     */
    @Query("SELECT MAX(d.totalScore) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true")
    Optional<Integer> findMaxScoreByUser(@Param("user") User user);

    @Query("SELECT MIN(d.totalScore) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true")
    Optional<Integer> findMinScoreByUser(@Param("user") User user);

    /**
     * 최근 N일 내 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.diagnosisDate >= :since ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findRecentDiagnoses(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * 최근 N일 내 진단 개수
     */
    @Query("SELECT COUNT(d) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.diagnosisDate >= :since")
    Long countRecentDiagnoses(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * 심각도별 진단 개수 조회
     */
    @Query("SELECT d.severity, COUNT(d) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "GROUP BY d.severity ORDER BY COUNT(d) DESC")
    List<Object[]> findSeverityDistribution(@Param("user") User user);

    /**
     * 가장 빈번한 심각도 조회
     */
    @Query("SELECT d.severity FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "GROUP BY d.severity ORDER BY COUNT(d) DESC LIMIT 1")
    Optional<DiagnosisSeverity> findMostFrequentSeverity(@Param("user") User user);

    /**
     * 긴급 처리가 필요한 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.severity IN ('SEVERE', 'VERY_SEVERE') ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findUrgentDiagnoses(@Param("user") User user);

    /**
     * 특정 점수 이상의 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.totalScore >= :minScore ORDER BY d.diagnosisDate DESC")
    List<Diagnosis> findByUserAndTotalScoreGreaterThanEqual(@Param("user") User user,
                                                            @Param("minScore") Integer minScore);

    /**
     * 미완료 진단 조회
     */
    @Query("SELECT d FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = false " +
           "ORDER BY d.createdAt DESC")
    List<Diagnosis> findIncompleteByUser(@Param("user") User user);

    /**
     * 특정 기간 내 완료된 진단 개수
     */
    @Query("SELECT COUNT(d) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "AND d.diagnosisDate BETWEEN :startDate AND :endDate")
    Long countCompletedDiagnosesBetween(@Param("user") User user,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 진단 점수 변화 추이 조회 (최근 N개)
     */
    @Query("SELECT d.totalScore, d.diagnosisDate FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "ORDER BY d.diagnosisDate DESC LIMIT :limit")
    List<Object[]> findScoreTrend(@Param("user") User user, @Param("limit") Integer limit);

    /**
     * 특정 날짜 이후 개선된 진단 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Diagnosis d " +
           "WHERE d.user = :user AND d.isCompleted = true AND d.diagnosisDate > :date " +
           "AND d.totalScore < :previousScore")
    Boolean hasImprovedSince(@Param("user") User user,
                             @Param("date") LocalDateTime date,
                             @Param("previousScore") Integer previousScore);

    /**
     * 전체 사용자 대비 사용자의 점수 순위 조회
     */
    @Query("SELECT COUNT(DISTINCT d.user) FROM Diagnosis d WHERE d.isCompleted = true " +
           "AND d.totalScore > (SELECT AVG(d2.totalScore) FROM Diagnosis d2 WHERE d2.user = :user AND d2.isCompleted = true)")
    Long findUserRanking(@Param("user") User user);

    /**
     * 시간대별 진단 패턴 분석
     */
    @Query("SELECT HOUR(d.diagnosisDate), COUNT(d) FROM Diagnosis d WHERE d.user = :user AND d.isCompleted = true " +
           "GROUP BY HOUR(d.diagnosisDate) ORDER BY COUNT(d) DESC")
    List<Object[]> findDiagnosisPattern(@Param("user") User user);

    List<Diagnosis> findByUser(User user);
}