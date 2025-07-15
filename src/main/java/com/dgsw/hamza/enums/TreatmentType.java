package com.dgsw.hamza.enums;

public enum TreatmentType {
    CBT("인지행동치료", "부정적 사고 패턴을 인식하고 변화시키는 치료법"),
    MEDITATION("명상/마음챙김", "현재 순간에 집중하여 마음의 평온을 찾는 치료법"),
    RELAXATION("이완요법", "몸과 마음의 긴장을 풀어주는 치료법"),
    EXERCISE("운동요법", "신체활동을 통한 정신건강 증진 치료법"),
    MEDICATION("약물치료", "정신건강 전문의 처방을 통한 약물 치료법");

    private final String displayName;
    private final String description;

    TreatmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}