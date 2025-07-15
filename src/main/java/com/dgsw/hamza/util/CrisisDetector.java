package com.dgsw.hamza.util;

import com.dgsw.hamza.enums.CrisisLevel;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class CrisisDetector {

    private CrisisDetector() {
        // Utility class - prevent instantiation
    }

    // 위기 상황 키워드 정의
    private static final List<String> CRITICAL_KEYWORDS = Arrays.asList(
            "자살", "죽고싶어", "죽고 싶어", "자해", "죽음", "목숨", "끝내고 싶어", "사라지고 싶어",
            "숨쉬기 힘들어", "더 이상 못 살겠어", "포기하고 싶어", "의미없어", "희망없어"
    );

    private static final List<String> HIGH_RISK_KEYWORDS = Arrays.asList(
            "절망", "외로워", "혼자", "무력감", "공허", "우울", "불안", "공황", "두려워",
            "아무도 모르겠어", "도움이 안 돼", "소용없어", "힘들어", "괴로워", "고통"
    );

    private static final List<String> MEDIUM_RISK_KEYWORDS = Arrays.asList(
            "스트레스", "피곤", "지쳐", "답답", "막막", "걱정", "고민", "부담", "압박",
            "잠 못 자", "식욕없어", "집중 안 돼", "기분 안 좋아"
    );

    private static final List<String> LOW_RISK_KEYWORDS = Arrays.asList(
            "조금 힘들어", "가끔 우울해", "약간 불안해", "살짝 걱정돼", "조금 스트레스",
            "잠깐 기분이", "별로 안 좋아"
    );

    // 긍정적 키워드 (위기 수준 완화)
    private static final List<String> POSITIVE_KEYWORDS = Arrays.asList(
            "괜찮아", "좋아", "행복", "희망", "감사", "기쁘다", "웃음", "즐거워",
            "도움이 돼", "나아지고 있어", "회복", "치료", "상담"
    );

    // 도움 요청 키워드
    private static final List<String> HELP_SEEKING_KEYWORDS = Arrays.asList(
            "도움", "상담", "치료", "병원", "의사", "전문가", "약물", "처방",
            "어떻게 해야", "도와줘", "상담받고 싶어", "치료받고 싶어"
    );

    /**
     * 메시지에서 위기 수준 감지
     */
    public static CrisisLevel detectCrisisLevel(String message) {
        if (message == null || message.trim().isEmpty()) {
            return CrisisLevel.NONE;
        }

        String normalizedMessage = message.toLowerCase().trim();
        log.debug("위기 수준 감지 시작: {}", normalizedMessage);

        // 1. 즉각 조치 필요한 키워드 검사
        if (containsKeywords(normalizedMessage, CRITICAL_KEYWORDS)) {
            log.warn("위기 상황 감지: CRITICAL - {}", message);
            return CrisisLevel.CRITICAL;
        }

        // 2. 높은 위험 키워드 검사
        if (containsKeywords(normalizedMessage, HIGH_RISK_KEYWORDS)) {
            // 긍정적 키워드가 함께 있으면 수준 완화
            if (containsKeywords(normalizedMessage, POSITIVE_KEYWORDS)) {
                return CrisisLevel.MEDIUM;
            }
            log.warn("위기 상황 감지: HIGH - {}", message);
            return CrisisLevel.HIGH;
        }

        // 3. 중간 위험 키워드 검사
        if (containsKeywords(normalizedMessage, MEDIUM_RISK_KEYWORDS)) {
            // 도움 요청 키워드가 함께 있으면 긍정적 신호
            if (containsKeywords(normalizedMessage, HELP_SEEKING_KEYWORDS)) {
                return CrisisLevel.LOW;
            }
            return CrisisLevel.MEDIUM;
        }

        // 4. 낮은 위험 키워드 검사
        if (containsKeywords(normalizedMessage, LOW_RISK_KEYWORDS)) {
            return CrisisLevel.LOW;
        }

        return CrisisLevel.NONE;
    }

    /**
     * 위기 상황 응답 메시지 생성
     */
    public static String generateCrisisResponse(CrisisLevel level, String userName) {
        String name = userName != null ? userName : "회원";
        
        switch (level) {
            case CRITICAL:
                return String.format("%s님, 지금 매우 힘든 시간을 보내고 계시는 것 같습니다. " +
                        "혼자 견디지 마시고 즉시 전문가의 도움을 받으시기 바랍니다.\n\n" +
                        "📞 생명의전화: 1588-9191 (24시간)\n" +
                        "📞 청소년전화: 1388\n" +
                        "📞 정신건강위기상담전화: 1577-0199\n\n" +
                        "위급한 상황이라면 119에 신고하거나 가까운 응급실을 방문해 주세요.", name);

            case HIGH:
                return String.format("%s님, 많이 힘드시겠어요. 지금 느끼시는 감정이 충분히 이해됩니다. " +
                        "전문가와 상담을 받아보시는 것을 강력히 권합니다.\n\n" +
                        "📞 생명의전화: 1588-9191\n" +
                        "📞 정신건강상담전화: 1577-0199\n\n" +
                        "병원 찾기 기능을 통해 근처 정신건강의학과를 찾아보시거나, " +
                        "치료 추천 기능을 이용해 보세요.", name);

            case MEDIUM:
                return String.format("%s님, 요즘 스트레스가 많으시군요. 이런 감정을 느끼는 것은 자연스러운 일입니다. " +
                        "하지만 계속 지속된다면 전문가의 도움을 받아보시는 것이 좋겠어요.\n\n" +
                        "스트레스 관리를 위한 치료법들을 추천해드릴 수 있습니다. " +
                        "명상이나 이완요법부터 시작해보시는 것은 어떨까요?", name);

            case LOW:
                return String.format("%s님, 가끔 이런 기분이 드는 것은 정상입니다. " +
                        "스스로를 돌보는 것이 중요해요. 충분한 휴식과 가벼운 운동, " +
                        "좋아하는 활동을 해보세요.\n\n" +
                        "필요하시면 언제든 다시 대화하거나 치료 추천 서비스를 이용해 보세요.", name);

            default:
                return String.format("%s님, 안녕하세요! 오늘 기분은 어떠신가요? " +
                        "무엇을 도와드릴까요?", name);
        }
    }

    /**
     * 위기 상황 행동 권장사항 생성
     */
    public static String generateActionRecommendation(CrisisLevel level) {
        switch (level) {
            case CRITICAL:
                return "⚠️ 즉각적인 전문가 도움이 필요합니다:\n" +
                       "1. 즉시 119 신고 또는 응급실 방문\n" +
                       "2. 생명의전화 1588-9191 연결\n" +
                       "3. 믿을 만한 가족이나 친구에게 연락\n" +
                       "4. 혼자 있지 말고 안전한 곳으로 이동";

            case HIGH:
                return "🚨 빠른 전문가 상담이 필요합니다:\n" +
                       "1. 정신건강의학과 예약\n" +
                       "2. 생명의전화나 상담전화 이용\n" +
                       "3. 가족이나 친구에게 상황 공유\n" +
                       "4. 안전 계획 수립";

            case MEDIUM:
                return "💡 다음 방법들을 시도해보세요:\n" +
                       "1. 전문가 상담 고려\n" +
                       "2. 스트레스 관리 기법 실행\n" +
                       "3. 규칙적인 생활 패턴 유지\n" +
                       "4. 지원 시스템 활용";

            case LOW:
                return "✨ 자기 관리 방법들:\n" +
                       "1. 충분한 휴식과 수면\n" +
                       "2. 가벼운 운동이나 산책\n" +
                       "3. 좋아하는 활동 하기\n" +
                       "4. 사람들과의 소통 늘리기";

            default:
                return "😊 건강한 마음 관리:\n" +
                       "1. 규칙적인 생활 습관\n" +
                       "2. 균형잡힌 식사와 운동\n" +
                       "3. 스트레스 해소 활동\n" +
                       "4. 긍정적인 관계 유지";
        }
    }

    /**
     * 위기 상황 모니터링 메시지 생성
     */
    public static String generateMonitoringMessage(CrisisLevel level) {
        switch (level) {
            case CRITICAL:
                return "🔴 위기 상황 - 즉각 조치 필요";
            case HIGH:
                return "🟠 높은 위험 - 신속 대응 필요";
            case MEDIUM:
                return "🟡 중간 위험 - 관심 필요";
            case LOW:
                return "🟢 낮은 위험 - 관찰 필요";
            default:
                return "⚪ 정상 상태";
        }
    }

    /**
     * 연속된 위기 메시지 패턴 감지
     */
    public static boolean detectContinuousCrisis(List<String> recentMessages) {
        if (recentMessages == null || recentMessages.size() < 3) {
            return false;
        }

        int crisisCount = 0;
        for (String message : recentMessages) {
            CrisisLevel level = detectCrisisLevel(message);
            if (level.isHighRisk()) {
                crisisCount++;
            }
        }

        return crisisCount >= 2; // 3개 중 2개 이상이 위기 상황
    }

    /**
     * 응급 연락처 정보 제공
     */
    public static String getEmergencyContacts() {
        return "🆘 응급 연락처:\n\n" +
               "📞 생명의전화: 1588-9191 (24시간)\n" +
               "📞 청소년전화: 1388\n" +
               "📞 정신건강위기상담: 1577-0199\n" +
               "📞 응급실: 119\n" +
               "📞 경찰신고: 112\n\n" +
               "💡 온라인 상담:\n" +
               "- 생명의전화 온라인상담\n" +
               "- 청소년상담1388\n" +
               "- 정신건강복지센터";
    }

    /**
     * 감정 상태 추적을 위한 키워드 분석
     */
    public static EmotionAnalysis analyzeEmotion(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new EmotionAnalysis("중립", 0, CrisisLevel.NONE);
        }

        String normalizedMessage = message.toLowerCase().trim();
        
        // 부정적 감정 점수 계산
        int negativeScore = 0;
        if (containsKeywords(normalizedMessage, CRITICAL_KEYWORDS)) negativeScore += 4;
        if (containsKeywords(normalizedMessage, HIGH_RISK_KEYWORDS)) negativeScore += 3;
        if (containsKeywords(normalizedMessage, MEDIUM_RISK_KEYWORDS)) negativeScore += 2;
        if (containsKeywords(normalizedMessage, LOW_RISK_KEYWORDS)) negativeScore += 1;

        // 긍정적 감정 점수 계산
        int positiveScore = containsKeywords(normalizedMessage, POSITIVE_KEYWORDS) ? 2 : 0;

        // 최종 감정 점수 (음수일수록 부정적)
        int emotionScore = positiveScore - negativeScore;
        
        String emotionType = determineEmotionType(emotionScore);
        CrisisLevel crisisLevel = detectCrisisLevel(message);

        return new EmotionAnalysis(emotionType, emotionScore, crisisLevel);
    }

    // Private helper methods

    private static boolean containsKeywords(String message, List<String> keywords) {
        return keywords.stream().anyMatch(keyword -> 
            message.contains(keyword.toLowerCase()) || 
            Pattern.compile("\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b").matcher(message).find()
        );
    }

    private static String determineEmotionType(int score) {
        if (score >= 2) return "매우 긍정적";
        if (score >= 1) return "긍정적";
        if (score >= 0) return "중립";
        if (score >= -1) return "약간 부정적";
        if (score >= -2) return "부정적";
        if (score >= -3) return "매우 부정적";
        return "극도로 부정적";
    }

    /**
     * 감정 분석 결과 클래스
     */
    public static class EmotionAnalysis {
        private final String emotionType;
        private final int emotionScore;
        private final CrisisLevel crisisLevel;

        public EmotionAnalysis(String emotionType, int emotionScore, CrisisLevel crisisLevel) {
            this.emotionType = emotionType;
            this.emotionScore = emotionScore;
            this.crisisLevel = crisisLevel;
        }

        public String getEmotionType() {
            return emotionType;
        }

        public int getEmotionScore() {
            return emotionScore;
        }

        public CrisisLevel getCrisisLevel() {
            return crisisLevel;
        }

        public boolean isNegative() {
            return emotionScore < 0;
        }

        public boolean isPositive() {
            return emotionScore > 0;
        }
    }
}