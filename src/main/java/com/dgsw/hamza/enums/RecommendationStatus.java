package com.dgsw.hamza.enums;

public enum RecommendationStatus {
    PENDING("대기중"),
    ACTIVE("활성화"),
    COMPLETED("완료"),
    CANCELLED("취소됨"),
    PAUSED("일시중지");

    private final String description;

    RecommendationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}