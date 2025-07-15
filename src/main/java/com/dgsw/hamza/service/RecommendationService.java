package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.RecommendationDto;
import com.dgsw.hamza.dto.TreatmentDto;
import com.dgsw.hamza.entity.Diagnosis;
import com.dgsw.hamza.entity.Treatment;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import com.dgsw.hamza.enums.TreatmentType;
import com.dgsw.hamza.repository.DiagnosisRepository;
import com.dgsw.hamza.repository.TreatmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommendationService {

    private final DiagnosisRepository diagnosisRepository;
    private final TreatmentRepository treatmentRepository;

    /**
     * 진단 기반 치료 추천 생성 (간단 버전)
     */
    public RecommendationDto.RecommendationResponse generateRecommendations(
            User user, 
            RecommendationDto.RecommendationRequest request) {
        
        log.info("사용자 {} 진단 {} 기반 치료 추천 생성", user.getId(), request.getDiagnosisId());

        // 1. 진단 정보 조회
        Diagnosis diagnosis = diagnosisRepository.findById(request.getDiagnosisId())
                .orElseThrow(() -> new IllegalArgumentException("진단을 찾을 수 없습니다: " + request.getDiagnosisId()));

        if (!diagnosis.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        // 2. 간단한 치료법 추천
        List<RecommendationDto.TreatmentRecommendationInfo> recommendations = 
            generateSimpleRecommendations(diagnosis.getSeverity());

        // 3. 응답 생성
        return RecommendationDto.RecommendationResponse.builder()
                .diagnosisId(diagnosis.getId())
                .recommendations(recommendations)
                .recommendationDate(LocalDateTime.now())
                .build();
    }

    /**
     * 추천 히스토리 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public RecommendationDto.RecommendationHistoryResponse getRecommendationHistory(User user, Integer limit) {
        log.info("사용자 {} 추천 히스토리 조회", user.getId());

        return RecommendationDto.RecommendationHistoryResponse.builder()
                .recommendations(List.of())
                .totalCount(0L)
                .build();
    }

    /**
     * 추천 통계 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public RecommendationDto.RecommendationStats getRecommendationStats(User user) {
        log.info("사용자 {} 추천 통계 조회", user.getId());

        return RecommendationDto.RecommendationStats.builder()
                .totalRecommendations(0)
                .completedRecommendations(0)
                .typeStats(List.of())
                .build();
    }

    /**
     * 추천 상태 업데이트 (간단 버전)
     */
    public void updateRecommendationStatus(User user, RecommendationDto.RecommendationUpdateRequest request) {
        log.info("사용자 {} 추천 상태 업데이트", user.getId());
        // 간단한 구현
    }

    /**
     * 진단별 추천 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public RecommendationDto.RecommendationCombinationResponse getRecommendationByDiagnosis(User user, Long diagnosisId) {
        log.info("사용자 {} 진단 {} 추천 조회", user.getId(), diagnosisId);

        return RecommendationDto.RecommendationCombinationResponse.builder()
                .supportingTreatments(List.of()) // 필드명 수정
                .build();
    }

    // Private helper methods

    private List<RecommendationDto.TreatmentRecommendationInfo> generateSimpleRecommendations(DiagnosisSeverity severity) {
        switch (severity) {
            case NORMAL:
                return Arrays.asList(
                    createRecommendation(TreatmentType.MEDITATION, "일상 명상", 1),
                    createRecommendation(TreatmentType.EXERCISE, "가벼운 운동", 2)
                );
            case MILD:
                return Arrays.asList(
                    createRecommendation(TreatmentType.CBT, "인지행동치료", 1),
                    createRecommendation(TreatmentType.RELAXATION, "이완요법", 2),
                    createRecommendation(TreatmentType.EXERCISE, "규칙적 운동", 3)
                );
            case MODERATE:
                return Arrays.asList(
                    createRecommendation(TreatmentType.CBT, "전문 CBT", 1),
                    createRecommendation(TreatmentType.MEDITATION, "집중 명상", 2),
                    createRecommendation(TreatmentType.RELAXATION, "심화 이완", 3)
                );
            case SEVERE:
                return Arrays.asList(
                    createRecommendation(TreatmentType.CBT, "집중 CBT", 1),
                    createRecommendation(TreatmentType.MEDICATION, "약물치료 상담", 2),
                    createRecommendation(TreatmentType.RELAXATION, "전문 이완", 3)
                );
            case VERY_SEVERE:
                return Arrays.asList(
                    createRecommendation(TreatmentType.MEDICATION, "즉시 약물치료", 1),
                    createRecommendation(TreatmentType.CBT, "응급 CBT", 2)
                );
            default:
                return List.of();
        }
    }

    private RecommendationDto.TreatmentRecommendationInfo createRecommendation(
            TreatmentType type, String name, int priority) {
        return RecommendationDto.TreatmentRecommendationInfo.builder()
                .treatment(TreatmentDto.TreatmentInfo.builder().name(name).type(type).build()) // 필드명 및 타입 수정
                .priority(null) // 예시: 실제 우선순위 Enum/값 필요시 할당
                .reason(getReason(type))
                .build();
    }

    private String getDescription(TreatmentType type) {
        switch (type) {
            case CBT: return "부정적 사고 패턴을 개선하는 치료법";
            case MEDITATION: return "마음의 안정을 찾는 명상 요법";
            case RELAXATION: return "신체와 정신의 긴장을 풀어주는 이완법";
            case EXERCISE: return "신체 활동을 통한 우울증 개선";
            case MEDICATION: return "전문의 처방에 따른 약물 치료";
            default: return "전문 치료법";
        }
    }

    private int getDuration(TreatmentType type) {
        switch (type) {
            case CBT: return 60;
            case MEDITATION: return 20;
            case RELAXATION: return 30;
            case EXERCISE: return 45;
            case MEDICATION: return 30;
            default: return 30;
        }
    }

    private String getReason(TreatmentType type) {
        switch (type) {
            case CBT: return "현재 증상에 가장 효과적인 치료법입니다";
            case MEDITATION: return "스트레스 감소와 마음의 평온에 도움됩니다";
            case RELAXATION: return "긴장 완화와 수면 개선에 효과적입니다";
            case EXERCISE: return "자연스러운 세로토닌 분비를 촉진합니다";
            case MEDICATION: return "전문의 판단하에 필요한 치료입니다";
            default: return "권장되는 치료법입니다";
        }
    }

    private List<String> getExplanations(DiagnosisSeverity severity) {
        return Arrays.asList(
            "현재 진단 결과에 따라 개인화된 치료법을 추천합니다.",
            "추천된 치료법들은 효과성과 접근성을 고려하여 선정되었습니다.",
            "치료법은 우선순위에 따라 단계적으로 시도해보시기 바랍니다.",
            "지속적인 실천이 중요하며, 전문가 상담을 병행하시기 바랍니다."
        );
    }
}