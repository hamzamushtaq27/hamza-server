package com.dgsw.hamza.enums;

public enum SeverityLevel {
    NORMAL("정상"),
    MILD("경미"),
    MODERATE("중등도"),
    SEVERE("심각"),
    VERY_SEVERE("매우 심각");

    private final String description;

    SeverityLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}