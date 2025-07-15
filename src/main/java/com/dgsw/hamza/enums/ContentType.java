package com.dgsw.hamza.enums;

public enum ContentType {
    TEXT("텍스트 콘텐츠"),
    AUDIO("오디오 콘텐츠"),
    VIDEO("비디오 콘텐츠"),
    IMAGE("이미지 콘텐츠"),
    INTERACTIVE("인터랙티브 콘텐츠");

    private final String description;

    ContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}