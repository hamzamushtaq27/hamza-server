package com.dgsw.hamza.util;

import com.dgsw.hamza.entity.DiagnosisAnswer;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DiagnosisCalculator {

    private DiagnosisCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * PHQ-9 총 점수 계산
     * @param answers 진단 답변 목록
     * @return 총 점수
     */
    public static int calculateTotalScore(List<DiagnosisAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return 0;
        }

        int totalScore = answers.stream()
                .mapToInt(DiagnosisAnswer::getAnswerScore)
                .sum();

        log.debug("PHQ-9 총 점수 계산: {} (답변 개수: {})", totalScore, answers.size());
        return totalScore;
    }

    /**
     * PHQ-9 점수를 기반으로 심각도 분류
     * @param totalScore 총 점수
     * @return 심각도 레벨
     */
    public static DiagnosisSeverity calculateSeverityLevel(int totalScore) {
        return DiagnosisSeverity.fromScore(totalScore);
    }

    /**
     * 심각도 레벨에 따른 한국어 설명
     * @param severityLevel 심각도 레벨
     * @return 한국어 설명
     */
    public static String getSeverityDescription(DiagnosisSeverity severityLevel) {
        switch (severityLevel) {
            case NORMAL:
                return "정상 - 우울증 증상이 거의 없습니다";
            case MILD:
                return "경미 - 가벼운 우울증 증상이 있습니다";
            case MODERATE:
                return "중등도 - 중간 정도의 우울증 증상이 있습니다";
            case SEVERE:
                return "심각 - 심한 우울증 증상이 있습니다";
            case VERY_SEVERE:
                return "매우 심각 - 매우 심한 우울증 증상이 있습니다";
            default:
                return "알 수 없음";
        }
    }

    /**
     * 심각도 레벨에 따른 권장사항
     * @param severityLevel 심각도 레벨
     * @return 권장사항
     */
    public static String getRecommendation(DiagnosisSeverity severityLevel) {
        switch (severityLevel) {
            case NORMAL:
                return "건강한 생활 습관을 유지하세요. 정기적인 운동과 충분한 수면을 권장합니다.";
            case MILD:
                return "스트레스 관리와 자기관리에 신경쓰세요. 필요시 상담을 고려해보세요.";
            case MODERATE:
                return "전문가 상담을 받아보시기 바랍니다. 인지행동치료나 상담치료가 도움이 될 수 있습니다.";
            case SEVERE:
                return "즉시 정신건강 전문가의 도움을 받으시기 바랍니다. 약물치료와 상담치료가 필요할 수 있습니다.";
            case VERY_SEVERE:
                return "긴급하게 정신건강 전문가의 도움이 필요합니다. 즉시 병원을 방문하거나 응급상황 시 119에 신고하세요.";
            default:
                return "전문가와 상담하시기 바랍니다.";
        }
    }

    /**
     * 점수 변화 분석
     * @param previousScore 이전 점수
     * @param currentScore 현재 점수
     * @return 분석 결과
     */
    public static String analyzeScoreChange(int previousScore, int currentScore) {
        int difference = currentScore - previousScore;
        
        if (difference == 0) {
            return "이전 진단과 동일한 수준입니다.";
        } else if (difference > 0) {
            if (difference >= 5) {
                return String.format("이전 진단보다 %d점 높아져 상태가 악화되었습니다.", difference);
            } else {
                return String.format("이전 진단보다 %d점 높아졌습니다.", difference);
            }
        } else {
            if (Math.abs(difference) >= 5) {
                return String.format("이전 진단보다 %d점 낮아져 상태가 개선되었습니다.", Math.abs(difference));
            } else {
                return String.format("이전 진단보다 %d점 낮아졌습니다.", Math.abs(difference));
            }
        }
    }

    /**
     * 위험도 색상 코드 반환
     * @param severityLevel 심각도 레벨
     * @return 색상 코드
     */
    public static String getSeverityColor(DiagnosisSeverity severityLevel) {
        switch (severityLevel) {
            case NORMAL:
                return "#4CAF50"; // 녹색
            case MILD:
                return "#FFC107"; // 노란색
            case MODERATE:
                return "#FF9800"; // 주황색
            case SEVERE:
                return "#F44336"; // 빨간색
            case VERY_SEVERE:
                return "#9C27B0"; // 보라색
            default:
                return "#9E9E9E"; // 회색
        }
    }

    /**
     * 재진단 필요 여부 판단
     * @param severityLevel 심각도 레벨
     * @param daysSinceLastDiagnosis 마지막 진단 이후 경과 일수
     * @return 재진단 필요 여부
     */
    public static boolean isRedoRequired(DiagnosisSeverity severityLevel, int daysSinceLastDiagnosis) {
        switch (severityLevel) {
            case NORMAL:
                return daysSinceLastDiagnosis >= 30; // 30일
            case MILD:
                return daysSinceLastDiagnosis >= 14; // 14일
            case MODERATE:
                return daysSinceLastDiagnosis >= 7;  // 7일
            case SEVERE:
            case VERY_SEVERE:
                return daysSinceLastDiagnosis >= 3;  // 3일
            default:
                return daysSinceLastDiagnosis >= 7;
        }
    }

    /**
     * 다음 진단 권장 일자 계산
     * @param severityLevel 심각도 레벨
     * @return 권장 일수
     */
    public static int getRecommendedRediagnosisDays(DiagnosisSeverity severityLevel) {
        switch (severityLevel) {
            case NORMAL:
                return 30;
            case MILD:
                return 14;
            case MODERATE:
                return 7;
            case SEVERE:
            case VERY_SEVERE:
                return 3;
            default:
                return 7;
        }
    }

    /**
     * 점수 유효성 검증
     * @param score 점수
     * @param maxQuestions 최대 문항 수
     * @return 유효성 여부
     */
    public static boolean isValidScore(int score, int maxQuestions) {
        return score >= 0 && score <= (maxQuestions * 3);
    }

    /**
     * 개별 답변 점수 유효성 검증
     * @param answerScore 답변 점수
     * @return 유효성 여부
     */
    public static boolean isValidAnswerScore(int answerScore) {
        return answerScore >= 0 && answerScore <= 3;
    }
}