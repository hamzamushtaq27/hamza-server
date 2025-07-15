package com.dgsw.hamza.enums;

public enum CrisisLevel {
    NONE("위기 없음", 0),
    LOW("낮은 위험", 1),
    MEDIUM("중간 위험", 2),
    HIGH("높은 위험", 3),
    CRITICAL("즉각 조치 필요", 4);

    private final String description;
    private final int level;

    CrisisLevel(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHighRisk() {
        return level >= 3;
    }
}