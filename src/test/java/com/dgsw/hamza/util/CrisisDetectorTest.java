package com.dgsw.hamza.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CrisisDetector 테스트")
class CrisisDetectorTest {

    private final CrisisDetector crisisDetector = new CrisisDetector();

    @Test
    @DisplayName("위기 상황 감지 - CRITICAL 레벨")
    void detectCrisis_Critical() {
        // given
        String criticalMessage = "죽고 싶어요. 더 이상 살 이유가 없어요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(criticalMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.CRITICAL);
    }

    @Test
    @DisplayName("위기 상황 감지 - HIGH 레벨")
    void detectCrisis_High() {
        // given
        String highMessage = "모든 것이 절망적이에요. 포기하고 싶어요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(highMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.HIGH);
    }

    @Test
    @DisplayName("위기 상황 감지 - MEDIUM 레벨")
    void detectCrisis_Medium() {
        // given
        String mediumMessage = "우울하고 힘들어요. 도움이 필요해요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(mediumMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.MEDIUM);
    }

    @Test
    @DisplayName("위기 상황 감지 - LOW 레벨")
    void detectCrisis_Low() {
        // given
        String lowMessage = "오늘은 조금 슬프네요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(lowMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.LOW);
    }

    @Test
    @DisplayName("위기 상황 감지 - NONE 레벨")
    void detectCrisis_None() {
        // given
        String normalMessage = "오늘 날씨가 좋네요. 기분이 좋아요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(normalMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.NONE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"자살", "죽고싶어", "살기싫어", "끝내고싶어", "자해"})
    @DisplayName("자살 관련 키워드 감지 테스트")
    void detectSuicideKeywords(String keyword) {
        // given
        String message = "저는 " + keyword + "를 생각하고 있어요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(message);

        // then
        assertThat(result).isIn(CrisisDetector.CrisisLevel.CRITICAL, CrisisDetector.CrisisLevel.HIGH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"우울", "슬픔", "외로움", "스트레스", "불안"})
    @DisplayName("일반 감정 키워드 감지 테스트")
    void detectEmotionalKeywords(String keyword) {
        // given
        String message = "요즘 " + keyword + "을 많이 느껴요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(message);

        // then
        assertThat(result).isIn(
                CrisisDetector.CrisisLevel.LOW, 
                CrisisDetector.CrisisLevel.MEDIUM, 
                CrisisDetector.CrisisLevel.NONE
        );
    }

    @Test
    @DisplayName("빈 문자열 처리 테스트")
    void detectCrisis_EmptyString() {
        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis("");

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.NONE);
    }

    @Test
    @DisplayName("null 문자열 처리 테스트")
    void detectCrisis_NullString() {
        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(null);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.NONE);
    }

    @Test
    @DisplayName("대소문자 구분 없는 키워드 감지 테스트")
    void detectCrisis_CaseInsensitive() {
        // given
        String upperCaseMessage = "죽고싶어요";
        String lowerCaseMessage = "죽고싶어요";

        // when
        CrisisDetector.CrisisLevel upperResult = crisisDetector.detectCrisis(upperCaseMessage);
        CrisisDetector.CrisisLevel lowerResult = crisisDetector.detectCrisis(lowerCaseMessage);

        // then
        assertThat(upperResult).isEqualTo(lowerResult);
        assertThat(upperResult).isEqualTo(CrisisDetector.CrisisLevel.CRITICAL);
    }

    @Test
    @DisplayName("복합 키워드 감지 테스트")
    void detectCrisis_MultipleKeywords() {
        // given
        String complexMessage = "너무 우울하고 절망적이에요. 죽고 싶어요.";

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(complexMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.CRITICAL);
    }
}