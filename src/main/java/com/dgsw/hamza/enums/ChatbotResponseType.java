package com.dgsw.hamza.enums;

public enum ChatbotResponseType {
    EMPATHY("공감적 응답"),
    GUIDANCE("지도적 응답"),
    INFORMATION("정보 제공"),
    CRISIS("위기 상황 대응"),
    GREETING("인사"),
    GENERAL("일반 응답");

    private final String description;

    ChatbotResponseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}