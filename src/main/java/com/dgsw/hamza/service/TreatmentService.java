package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.TreatmentDto;
import com.dgsw.hamza.entity.Treatment;
import com.dgsw.hamza.entity.TreatmentContent;
import com.dgsw.hamza.enums.TreatmentType;
import com.dgsw.hamza.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;

    /**
     * 활성화된 모든 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getActiveTreatments() {
        log.info("활성화된 치료법 목록 조회");
        
        List<Treatment> treatments = treatmentRepository.findActiveTreatments();
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 치료법 목록 조회 (필터링 및 페이징)
     */
    @Transactional(readOnly = true)
    public TreatmentDto.TreatmentListResponse getTreatmentList(TreatmentDto.TreatmentListRequest request) {
        log.info("치료법 목록 조회 - 유형: {}, 난이도: {}, 최대시간: {}", 
                request.getType(), request.getDifficultyLevel(), request.getMaxDuration());
        
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10
        );
        
        Page<Treatment> treatmentPage = treatmentRepository.findTreatmentsWithFilters(
                request.getType(),
                request.getDifficultyLevel(),
                request.getMaxDuration(),
                pageable
        );
        
        List<TreatmentDto.TreatmentInfo> treatments = treatmentPage.getContent().stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
        
        return TreatmentDto.TreatmentListResponse.builder()
                .treatments(treatments)
                .totalCount(treatmentPage.getTotalElements())
                .currentPage(treatmentPage.getNumber())
                .totalPages(treatmentPage.getTotalPages())
                .hasNext(treatmentPage.hasNext())
                .build();
    }

    /**
     * 특정 치료법 상세 조회
     */
    @Transactional(readOnly = true)
    public TreatmentDto.TreatmentInfo getTreatmentDetail(Long treatmentId) {
        log.info("치료법 상세 조회 - ID: {}", treatmentId);
        
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("치료법을 찾을 수 없습니다: " + treatmentId));
        
        return convertToTreatmentInfo(treatment);
    }

    /**
     * 치료법 유형별 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getTreatmentsByType(TreatmentType type) {
        log.info("치료법 유형별 조회 - 유형: {}", type);
        
        List<Treatment> treatments = treatmentRepository.findByTypeAndActive(type);
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 치료법 검색
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> searchTreatments(String keyword) {
        log.info("치료법 검색 - 키워드: {}", keyword);
        
        List<Treatment> treatments = treatmentRepository.searchTreatments(keyword);
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 치료법 통계 조회
     */
    @Transactional(readOnly = true)
    public TreatmentDto.TreatmentStatsResponse getTreatmentStats() {
        log.info("치료법 통계 조회");
        
        // 전체 치료법 수
        Long totalTreatments = treatmentRepository.count();
        Long activeTreatments = treatmentRepository.countActiveTreatments();
        
        // 평균 소요시간
        Double averageDuration = treatmentRepository.findAverageDuration();
        
        // 치료법 유형별 통계
        List<Object[]> typeCountData = treatmentRepository.countByType();
        List<TreatmentDto.TreatmentTypeStats> typeStats = typeCountData.stream()
                .map(data -> {
                    TreatmentType type = (TreatmentType) data[0];
                    Long count = (Long) data[1];
                    Double avgDuration = treatmentRepository.findAverageDurationByType(type);
                    
                    return TreatmentDto.TreatmentTypeStats.builder()
                            .type(type)
                            .count(count.intValue())
                            .averageDuration(avgDuration)
                            .popularityScore(calculatePopularityScore(type))
                            .build();
                })
                .collect(Collectors.toList());
        
        // 가장 인기있는 치료법
        TreatmentDto.TreatmentInfo mostPopularTreatment = getMostPopularTreatment();
        
        return TreatmentDto.TreatmentStatsResponse.builder()
                .totalTreatments(totalTreatments.intValue())
                .activeTreatments(activeTreatments.intValue())
                .typeStats(typeStats)
                .averageDuration(averageDuration)
                .mostPopularTreatment(mostPopularTreatment)
                .build();
    }

    /**
     * 치료법 컨텐츠 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentContentInfo> getTreatmentContents(Long treatmentId) {
        log.info("치료법 컨텐츠 조회 - 치료법 ID: {}", treatmentId);
        
        Treatment treatment = treatmentRepository.findById(treatmentId)
                .orElseThrow(() -> new IllegalArgumentException("치료법을 찾을 수 없습니다: " + treatmentId));
        
        return treatment.getContents().stream()
                .filter(TreatmentContent::isActive)
                .sorted((c1, c2) -> Integer.compare(c1.getSequenceOrder(), c2.getSequenceOrder()))
                .map(this::convertToContentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 추천 가능한 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<Treatment> getRecommendableTreatments() {
        log.info("추천 가능한 치료법 조회");
        return treatmentRepository.findRecommendableTreatments();
    }

    /**
     * 사용자별 추천 이력 기반 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getNotRecommendedTreatments(Long userId) {
        log.info("사용자 {} 미추천 치료법 조회", userId);
        
        List<Treatment> treatments = treatmentRepository.findNotRecommendedTreatments(userId);
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 인기 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getPopularTreatments(Integer limit) {
        log.info("인기 치료법 조회 (limit: {})", limit);
        
        List<Object[]> popularData = treatmentRepository.findPopularTreatments();
        
        return popularData.stream()
                .limit(limit != null ? limit : 10)
                .map(data -> {
                    Treatment treatment = (Treatment) data[0];
                    return convertToTreatmentInfo(treatment);
                })
                .collect(Collectors.toList());
    }

    /**
     * 소요시간 기반 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getTreatmentsByDuration(Integer maxDuration) {
        log.info("소요시간 기반 치료법 조회 - 최대 {}분", maxDuration);
        
        List<Treatment> treatments = treatmentRepository.findByDurationLessThanEqual(maxDuration);
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    /**
     * 난이도별 치료법 조회
     */
    @Transactional(readOnly = true)
    public List<TreatmentDto.TreatmentInfo> getTreatmentsByDifficulty(String difficultyLevel) {
        log.info("난이도별 치료법 조회 - 난이도: {}", difficultyLevel);
        
        List<Treatment> treatments = treatmentRepository.findByDifficultyLevelAndActive(difficultyLevel);
        
        return treatments.stream()
                .map(this::convertToTreatmentInfo)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private TreatmentDto.TreatmentInfo convertToTreatmentInfo(Treatment treatment) {
        return TreatmentDto.TreatmentInfo.builder()
                .treatmentId(treatment.getId())
                .name(treatment.getName())
                .description(treatment.getDescription())
                .type(treatment.getType())
                .durationMinutes(treatment.getDurationMinutes())
                .difficultyLevel(String.valueOf(treatment.getDifficultyLevel()))
                .isActive(treatment.isActive())
                .createdAt(treatment.getCreatedAt())
                .updatedAt(treatment.getUpdatedAt())
                .build();
    }

    private TreatmentDto.TreatmentContentInfo convertToContentInfo(TreatmentContent content) {
        return TreatmentDto.TreatmentContentInfo.builder()
                .contentId(content.getId())
                .title(content.getTitle())
                .content(content.getContent())
                .contentType(String.valueOf(content.getContentType()))
                .sequenceOrder(content.getSequenceOrder())
                .isActive(content.isActive())
                .build();
    }

    private Double calculatePopularityScore(TreatmentType type) {
        // 추천 받은 횟수 기반으로 인기도 점수 계산
        // 실제 구현에서는 더 복잡한 로직 적용 가능
        List<Object[]> popularData = treatmentRepository.findPopularTreatments();
        
        return popularData.stream()
                .filter(data -> {
                    Treatment treatment = (Treatment) data[0];
                    return treatment.getType().equals(type);
                })
                .mapToDouble(data -> {
                    Long count = (Long) data[1];
                    return count.doubleValue();
                })
                .average()
                .orElse(0.0);
    }

    private TreatmentDto.TreatmentInfo getMostPopularTreatment() {
        List<Object[]> popularData = treatmentRepository.findPopularTreatments();
        
        if (popularData.isEmpty()) {
            return null;
        }
        
        Treatment mostPopular = (Treatment) popularData.get(0)[0];
        return convertToTreatmentInfo(mostPopular);
    }
}