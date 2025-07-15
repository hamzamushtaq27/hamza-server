package com.dgsw.hamza.enums;

public enum RecommendationPriority {
    URGENT("긴급", 1),
    HIGH("높음", 2),
    MEDIUM("보통", 3),
    LOW("낮음", 4);

    private final String displayName;
    private final int priority;

    RecommendationPriority(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }
}