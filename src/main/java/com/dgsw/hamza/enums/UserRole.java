package com.dgsw.hamza.enums;

public enum UserRole {
    USER("일반 사용자"),
    ADMIN("관리자"),
    THERAPIST("상담사");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}