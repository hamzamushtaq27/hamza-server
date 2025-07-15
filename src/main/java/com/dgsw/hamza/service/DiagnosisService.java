package com.dgsw.hamza.service;

import com.dgsw.hamza.config.CacheConfig;
import com.dgsw.hamza.dto.DiagnosisDto;
import com.dgsw.hamza.dto.PageRequest;
import com.dgsw.hamza.dto.PageResponse;
import com.dgsw.hamza.entity.Diagnosis;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import com.dgsw.hamza.repository.DiagnosisRepository;
import com.dgsw.hamza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;

    /**
     * PHQ-9 설문 문항 조회 (하드코딩)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.DIAGNOSIS_QUESTIONS_CACHE, key = "'phq9_questions'")
    public List<DiagnosisDto.QuestionResponse> getQuestions() {
        log.info("PHQ-9 설문 문항 조회");
        
        return Arrays.asList(
            DiagnosisDto.QuestionResponse.builder()
                .questionId(1L)
                .questionText("지난 2주 동안 일 또는 다른 활동을 하는 데 흥미나 즐거움을 느끼지 못했다")
                .questionOrder(1)
                .isActive(true)
                .build(),
            DiagnosisDto.QuestionResponse.builder()
                .questionId(2L)
                .questionText("지난 2주 동안 기분이 가라앉거나 우울하거나 절망적으로 느꼈다")
                .questionOrder(2)
                .isActive(true)
                .build(),
            DiagnosisDto.QuestionResponse.builder()
                .questionId(3L)
                .questionText("지난 2주 동안 잠들기가 어렵거나 자주 깨거나 반대로 너무 많이 잠을 잤다")
                .questionOrder(3)
                .isActive(true)
                .build(),
            DiagnosisDto.QuestionResponse.builder()
                .questionId(4L)
                .questionText("지난 2주 동안 피곤하다고 느끼거나 에너지가 거의 없었다")
                .questionOrder(4)
                .isActive(true)
                .build(),
            DiagnosisDto.QuestionResponse.builder()
                .questionId(5L)
                .questionText("지난 2주 동안 자신에 대해 나쁘게 생각하거나 자신이 실패자라고 느꼈다")
                .questionOrder(5)
                .isActive(true)
                .build()
        );
    }

    /**
     * 진단 설문 제출 및 결과 계산 (간단 버전)
     */
    public DiagnosisDto.DiagnosisResponse submitDiagnosis(User user, DiagnosisDto.DiagnosisSubmitRequest request) {
        log.info("사용자 {} 진단 설문 제출", user.getId());

        // 1. 답변 유효성 검증
        validateAnswers(request.getAnswers());

        // 2. 진단 엔티티 생성
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setUser(user);
        diagnosis.setNotes(request.getNotes());

        // 3. 점수 계산
        int totalScore = request.getAnswers().stream()
                .mapToInt(DiagnosisDto.DiagnosisAnswerRequest::getScore)
                .sum();
        
        DiagnosisSeverity severity = calculateSeverity(totalScore);

        // 4. 진단 완료 처리
        diagnosis.setTotalScore(totalScore);
        diagnosis.setSeverity(severity);

        diagnosisRepository.save(diagnosis);

        // 5. 응답 생성
        return DiagnosisDto.DiagnosisResponse.builder()
                .diagnosisId(diagnosis.getId())
                .totalScore(totalScore)
                .severity(severity)
                .severityDescription(getSeverityDescription(severity))
                .recommendation(getRecommendation(severity))
                .severityColor(getSeverityColor(severity))
                .diagnosisDate(LocalDateTime.now())
                .scoreComparison("새로운 진단입니다.")
                .requiresImmediateAttention(severity == DiagnosisSeverity.VERY_SEVERE)
                .recommendedRediagnosisDays(getRecommendedRediagnosisDays(severity))
                .notes(diagnosis.getNotes())
                .build();
    }

    /**
     * 진단 히스토리 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public DiagnosisDto.DiagnosisHistoryResponse getDiagnosisHistory(User user, Integer limit) {
        log.info("사용자 {} 진단 히스토리 조회", user.getId());

        List<Diagnosis> diagnoses = diagnosisRepository.findByUser(user);
        
        return DiagnosisDto.DiagnosisHistoryResponse.builder()
                .diagnoses(List.of())
                .totalCount(diagnoses.size())
                .build();
    }

    /**
     * 진단 상세 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public DiagnosisDto.DiagnosisResponse getDiagnosisDetails(User user, Long diagnosisId) {
        log.info("사용자 {} 진단 {} 상세 조회", user.getId(), diagnosisId);

        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new IllegalArgumentException("진단을 찾을 수 없습니다: " + diagnosisId));

        if (!diagnosis.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }

        return DiagnosisDto.DiagnosisResponse.builder()
                .diagnosisId(diagnosis.getId())
                .totalScore(diagnosis.getTotalScore())
                .severity(diagnosis.getSeverity())
                .severityDescription(getSeverityDescription(diagnosis.getSeverity()))
                .recommendation(getRecommendation(diagnosis.getSeverity()))
                .severityColor(getSeverityColor(diagnosis.getSeverity()))
                .diagnosisDate(diagnosis.getCreatedAt())
                .scoreComparison("이전 진단과 비교")
                .requiresImmediateAttention(diagnosis.getSeverity() == DiagnosisSeverity.VERY_SEVERE)
                .recommendedRediagnosisDays(getRecommendedRediagnosisDays(diagnosis.getSeverity()))
                .notes(diagnosis.getNotes())
                .build();
    }

    /**
     * 진단 통계 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public DiagnosisDto.DiagnosisStatsResponse getDiagnosisStats(User user) {
        log.info("사용자 {} 진단 통계 조회", user.getId());

        Long totalDiagnoses = diagnosisRepository.countByUser(user);
        
        return DiagnosisDto.DiagnosisStatsResponse.builder()
                .totalDiagnoses(totalDiagnoses.intValue())
                .averageScore(0.0)
                .recentDiagnoses(0)
                .mostFrequentSeverity(DiagnosisSeverity.NORMAL)
                .severityDistribution(List.of())
                .build();
    }

    /**
     * 진단 히스토리 페이지네이션 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<DiagnosisDto.DiagnosisHistoryItem> getDiagnosisHistoryPaged(User user, PageRequest pageRequest) {
        log.info("사용자 {} 진단 히스토리 페이지네이션 조회", user.getId());

        Page<Diagnosis> diagnosisPage = diagnosisRepository.findByUserOrderByCreatedAtDesc(
                user, pageRequest.toPageable());

        return PageResponse.of(diagnosisPage.map(diagnosis -> 
                DiagnosisDto.DiagnosisHistoryItem.builder()
                        .diagnosisId(diagnosis.getId())
                        .totalScore(diagnosis.getTotalScore())
                        .severity(diagnosis.getSeverity())
                        .diagnosisDate(diagnosis.getCreatedAt())
                        .build()));
    }

    // Private helper methods

    private void validateAnswers(List<DiagnosisDto.DiagnosisAnswerRequest> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("답변이 없습니다.");
        }

        for (DiagnosisDto.DiagnosisAnswerRequest answer : answers) {
            if (answer.getScore() < 0 || answer.getScore() > 3) {
                throw new IllegalArgumentException("유효하지 않은 답변 점수입니다: " + answer.getScore());
            }

            if (answer.getQuestionId() < 1 || answer.getQuestionId() > 5) {
                throw new IllegalArgumentException("유효하지 않은 문항 번호입니다: " + answer.getQuestionId());
            }
        }
    }

    private DiagnosisSeverity calculateSeverity(int totalScore) {
        if (totalScore <= 4) return DiagnosisSeverity.NORMAL;
        if (totalScore <= 9) return DiagnosisSeverity.MILD;
        if (totalScore <= 14) return DiagnosisSeverity.MODERATE;
        if (totalScore <= 19) return DiagnosisSeverity.SEVERE;
        return DiagnosisSeverity.VERY_SEVERE;
    }

    private String getSeverityDescription(DiagnosisSeverity severity) {
        switch (severity) {
            case NORMAL: return "정상 범위";
            case MILD: return "경미한 우울증";
            case MODERATE: return "중간 정도 우울증";
            case SEVERE: return "심한 우울증";
            case VERY_SEVERE: return "매우 심한 우울증";
            default: return "알 수 없음";
        }
    }

    private String getRecommendation(DiagnosisSeverity severity) {
        switch (severity) {
            case NORMAL: 
                return "현재 상태가 양호합니다. 건강한 생활 습관을 유지하세요.";
            case MILD: 
                return "가벼운 우울 증상이 있습니다. 충분한 휴식과 운동을 권장합니다.";
            case MODERATE: 
                return "중간 정도의 우울 증상이 있습니다. 전문가 상담을 고려해보세요.";
            case SEVERE: 
                return "심한 우울 증상이 있습니다. 즉시 전문의 진료를 받으시기 바랍니다.";
            case VERY_SEVERE: 
                return "매우 심한 우울 증상이 있습니다. 즉시 응급 치료가 필요합니다.";
            default: 
                return "전문가와 상담하시기 바랍니다.";
        }
    }

    private String getSeverityColor(DiagnosisSeverity severity) {
        switch (severity) {
            case NORMAL: return "#4CAF50";
            case MILD: return "#FFC107";
            case MODERATE: return "#FF9800";
            case SEVERE: return "#F44336";
            case VERY_SEVERE: return "#D32F2F";
            default: return "#9E9E9E";
        }
    }

    private int getRecommendedRediagnosisDays(DiagnosisSeverity severity) {
        switch (severity) {
            case NORMAL: return 90;
            case MILD: return 30;
            case MODERATE: return 14;
            case SEVERE: return 7;
            case VERY_SEVERE: return 3;
            default: return 30;
        }
    }
}