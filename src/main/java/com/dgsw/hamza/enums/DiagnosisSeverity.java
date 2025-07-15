package com.dgsw.hamza.enums;

public enum DiagnosisSeverity {
    NORMAL("정상", 0, 4),
    MILD("경미", 5, 9),
    MODERATE("중등도", 10, 14),
    SEVERE("심각", 15, 19),
    VERY_SEVERE("매우 심각", 20, 27);

    private final String description;
    private final int minScore;
    private final int maxScore;

    DiagnosisSeverity(String description, int minScore, int maxScore) {
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getDescription() {
        return description;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public static DiagnosisSeverity fromScore(int score) {
        for (DiagnosisSeverity severity : values()) {
            if (score >= severity.minScore && score <= severity.maxScore) {
                return severity;
            }
        }
        return NORMAL;
    }
}