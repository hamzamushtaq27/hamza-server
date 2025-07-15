package com.dgsw.hamza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treatment_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentProgram extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "sessions_per_week")
    private Integer sessionsPerWeek;

    @Column(name = "target_condition")
    private String targetCondition;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "prerequisites")
    private String prerequisites;

    @Column(name = "expected_outcomes")
    private String expectedOutcomes;

    @Column(name = "category")
    private String category;

    // Relationships
    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserProgress> userProgress = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "program_treatments",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "treatment_id")
    )
    @Builder.Default
    private List<Treatment> treatments = new ArrayList<>();

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isShortProgram() {
        return durationWeeks != null && durationWeeks <= 4;
    }

    public boolean isMediumProgram() {
        return durationWeeks != null && durationWeeks > 4 && durationWeeks <= 12;
    }

    public boolean isLongProgram() {
        return durationWeeks != null && durationWeeks > 12;
    }

    public int getTotalSessions() {
        if (durationWeeks == null || sessionsPerWeek == null) {
            return 0;
        }
        return durationWeeks * sessionsPerWeek;
    }

    public boolean isIntensive() {
        return sessionsPerWeek != null && sessionsPerWeek >= 5;
    }
}