package com.dgsw.hamza.enums;

public enum DifficultyLevel {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급"),
    EXPERT("전문가");

    private final String description;

    DifficultyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}