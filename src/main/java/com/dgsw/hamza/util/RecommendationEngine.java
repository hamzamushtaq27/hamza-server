package com.dgsw.hamza.util;

import com.dgsw.hamza.entity.Treatment;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import com.dgsw.hamza.enums.RecommendationPriority;
import com.dgsw.hamza.enums.TreatmentType;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class RecommendationEngine {

    private RecommendationEngine() {
        // Utility class - prevent instantiation
    }

    /**
     * 진단 심각도 기반 치료 추천
     */
    public static List<TreatmentRecommendation> recommendTreatments(
            DiagnosisSeverity severity, 
            List<Treatment> availableTreatments) {
        
        log.info("진단 심각도 {} 기반 치료 추천 시작", severity);
        
        Map<TreatmentType, Double> typeWeights = getTreatmentWeights(severity);
        
        return availableTreatments.stream()
                .map(treatment -> {
                    double score = calculateRecommendationScore(treatment, severity, typeWeights);
                    RecommendationPriority priority = calculatePriority(severity, treatment.getType());
                    
                    return new TreatmentRecommendation(
                            treatment,
                            score,
                            priority,
                            generateRecommendationReason(treatment, severity)
                    );
                })
                .sorted(Comparator.comparingDouble(TreatmentRecommendation::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 심각도별 치료 유형 가중치 계산
     */
    private static Map<TreatmentType, Double> getTreatmentWeights(DiagnosisSeverity severity) {
        Map<TreatmentType, Double> weights = new HashMap<>();
        
        switch (severity) {
            case NORMAL:
                weights.put(TreatmentType.MEDITATION, 0.9);
                weights.put(TreatmentType.EXERCISE, 0.8);
                weights.put(TreatmentType.RELAXATION, 0.7);
                weights.put(TreatmentType.CBT, 0.3);
                weights.put(TreatmentType.MEDICATION, 0.1);
                break;
                
            case MILD:
                weights.put(TreatmentType.CBT, 0.8);
                weights.put(TreatmentType.MEDITATION, 0.7);
                weights.put(TreatmentType.EXERCISE, 0.6);
                weights.put(TreatmentType.RELAXATION, 0.6);
                weights.put(TreatmentType.MEDICATION, 0.2);
                break;
                
            case MODERATE:
                weights.put(TreatmentType.CBT, 0.9);
                weights.put(TreatmentType.MEDITATION, 0.6);
                weights.put(TreatmentType.EXERCISE, 0.5);
                weights.put(TreatmentType.RELAXATION, 0.5);
                weights.put(TreatmentType.MEDICATION, 0.6);
                break;
                
            case SEVERE:
                weights.put(TreatmentType.CBT, 0.9);
                weights.put(TreatmentType.MEDICATION, 0.8);
                weights.put(TreatmentType.MEDITATION, 0.4);
                weights.put(TreatmentType.EXERCISE, 0.3);
                weights.put(TreatmentType.RELAXATION, 0.4);
                break;
                
            case VERY_SEVERE:
                weights.put(TreatmentType.MEDICATION, 0.9);
                weights.put(TreatmentType.CBT, 0.8);
                weights.put(TreatmentType.MEDITATION, 0.2);
                weights.put(TreatmentType.EXERCISE, 0.1);
                weights.put(TreatmentType.RELAXATION, 0.3);
                break;
        }
        
        return weights;
    }

    /**
     * 치료 추천 점수 계산
     */
    private static double calculateRecommendationScore(
            Treatment treatment, 
            DiagnosisSeverity severity,
            Map<TreatmentType, Double> typeWeights) {
        
        double baseScore = typeWeights.getOrDefault(treatment.getType(), 0.0);
        
        // 난이도 보정
        double difficultyFactor = getDifficultyFactor(String.valueOf(treatment.getDifficultyLevel()), severity);
        
        // 소요시간 보정
        double durationFactor = getDurationFactor(treatment.getDurationMinutes(), severity);
        
        // 활성화 상태 보정
        double activeFactor = treatment.isActive() ? 1.0 : 0.0;
        
        double finalScore = baseScore * difficultyFactor * durationFactor * activeFactor;
        
        log.debug("치료 {} 점수 계산: base={}, difficulty={}, duration={}, active={}, final={}", 
                treatment.getName(), baseScore, difficultyFactor, durationFactor, activeFactor, finalScore);
        
        return finalScore;
    }

    /**
     * 난이도 보정 계수 계산
     */
    private static double getDifficultyFactor(String difficultyLevel, DiagnosisSeverity severity) {
        if (difficultyLevel == null) return 1.0;
        
        switch (severity) {
            case NORMAL:
            case MILD:
                return "BEGINNER".equals(difficultyLevel) ? 1.0 : 
                       "INTERMEDIATE".equals(difficultyLevel) ? 0.8 : 0.6;
            case MODERATE:
                return "BEGINNER".equals(difficultyLevel) ? 0.9 : 
                       "INTERMEDIATE".equals(difficultyLevel) ? 1.0 : 0.8;
            case SEVERE:
            case VERY_SEVERE:
                return "BEGINNER".equals(difficultyLevel) ? 0.7 : 
                       "INTERMEDIATE".equals(difficultyLevel) ? 0.9 : 1.0;
            default:
                return 1.0;
        }
    }

    /**
     * 소요시간 보정 계수 계산
     */
    private static double getDurationFactor(Integer durationMinutes, DiagnosisSeverity severity) {
        if (durationMinutes == null) return 1.0;
        
        switch (severity) {
            case NORMAL:
            case MILD:
                return durationMinutes <= 15 ? 1.0 : 
                       durationMinutes <= 30 ? 0.9 : 0.8;
            case MODERATE:
                return durationMinutes <= 30 ? 1.0 : 
                       durationMinutes <= 60 ? 0.9 : 0.8;
            case SEVERE:
            case VERY_SEVERE:
                return durationMinutes <= 60 ? 1.0 : 0.9;
            default:
                return 1.0;
        }
    }

    /**
     * 우선순위 계산
     */
    private static RecommendationPriority calculatePriority(DiagnosisSeverity severity, TreatmentType treatmentType) {
        switch (severity) {
            case VERY_SEVERE:
                return treatmentType == TreatmentType.MEDICATION ? RecommendationPriority.URGENT : RecommendationPriority.HIGH;
            case SEVERE:
                return treatmentType == TreatmentType.CBT || treatmentType == TreatmentType.MEDICATION ? 
                       RecommendationPriority.HIGH : RecommendationPriority.MEDIUM;
            case MODERATE:
                return treatmentType == TreatmentType.CBT ? RecommendationPriority.HIGH : RecommendationPriority.MEDIUM;
            case MILD:
                return RecommendationPriority.MEDIUM;
            case NORMAL:
                return RecommendationPriority.LOW;
            default:
                return RecommendationPriority.MEDIUM;
        }
    }

    /**
     * 추천 이유 생성
     */
    private static String generateRecommendationReason(Treatment treatment, DiagnosisSeverity severity) {
        StringBuilder reason = new StringBuilder();
        
        switch (severity) {
            case NORMAL:
                reason.append("예방적 관리를 위해 ");
                break;
            case MILD:
                reason.append("경미한 우울 증상 완화를 위해 ");
                break;
            case MODERATE:
                reason.append("중등도 우울 증상 치료를 위해 ");
                break;
            case SEVERE:
                reason.append("심각한 우울 증상 치료를 위해 ");
                break;
            case VERY_SEVERE:
                reason.append("매우 심각한 우울 증상의 즉각적 치료를 위해 ");
                break;
        }
        
        switch (treatment.getType()) {
            case CBT:
                reason.append("부정적 사고 패턴을 개선하는 인지행동치료를 권장합니다.");
                break;
            case MEDITATION:
                reason.append("마음의 평온을 찾는 명상/마음챙김 치료를 권장합니다.");
                break;
            case RELAXATION:
                reason.append("몸과 마음의 긴장을 풀어주는 이완요법을 권장합니다.");
                break;
            case EXERCISE:
                reason.append("신체활동을 통한 정신건강 증진을 위한 운동요법을 권장합니다.");
                break;
            case MEDICATION:
                reason.append("전문의 진료를 통한 약물치료를 권장합니다.");
                break;
        }
        
        return reason.toString();
    }

    /**
     * 개인화 추천 (사용자 선호도 반영)
     */
    public static List<TreatmentRecommendation> personalizedRecommendations(
            List<TreatmentRecommendation> baseRecommendations,
            User user) {
        
        // 사용자 선호도나 과거 치료 이력을 반영한 개인화 로직
        // 현재는 기본 추천을 반환하지만, 향후 확장 가능
        
        return baseRecommendations.stream()
                .map(rec -> {
                    // 개인화 점수 조정 로직 추가 가능
                    return rec;
                })
                .collect(Collectors.toList());
    }

    /**
     * 치료 추천 결과 클래스
     */
    public static class TreatmentRecommendation {
        private final Treatment treatment;
        private final double score;
        private final RecommendationPriority priority;
        private final String reason;

        public TreatmentRecommendation(Treatment treatment, double score, 
                                     RecommendationPriority priority, String reason) {
            this.treatment = treatment;
            this.score = score;
            this.priority = priority;
            this.reason = reason;
        }

        public Treatment getTreatment() {
            return treatment;
        }

        public double getScore() {
            return score;
        }

        public RecommendationPriority getPriority() {
            return priority;
        }

        public String getReason() {
            return reason;
        }
    }

    /**
     * 최적 치료 조합 추천
     */
    public static List<TreatmentRecommendation> recommendTreatmentCombination(
            DiagnosisSeverity severity,
            List<Treatment> availableTreatments,
            int maxRecommendations) {
        
        List<TreatmentRecommendation> recommendations = recommendTreatments(severity, availableTreatments);
        
        // 치료 유형별로 최고 점수 치료법 선택
        Map<TreatmentType, TreatmentRecommendation> bestByType = new HashMap<>();
        
        for (TreatmentRecommendation rec : recommendations) {
            TreatmentType type = rec.getTreatment().getType();
            if (!bestByType.containsKey(type) || 
                bestByType.get(type).getScore() < rec.getScore()) {
                bestByType.put(type, rec);
            }
        }
        
        return bestByType.values().stream()
                .sorted(Comparator.comparingDouble(TreatmentRecommendation::getScore).reversed())
                .limit(maxRecommendations)
                .collect(Collectors.toList());
    }
}