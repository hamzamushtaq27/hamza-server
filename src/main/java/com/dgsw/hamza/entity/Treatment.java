package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.TreatmentType;
import com.dgsw.hamza.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "treatments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Treatment extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TreatmentType type;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "instruction")
    private String instruction;

    @Column(name = "prerequisites")
    private String prerequisites;

    @Column(name = "expected_outcomes")
    private String expectedOutcomes;

    @Column(name = "category")
    private String category;

    @Column(name = "tags")
    private String tags;

    // Relationships
    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TreatmentContent> contents = new ArrayList<>();

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "treatment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserProgress> userProgress = new ArrayList<>();

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isBeginnerFriendly() {
        return difficultyLevel == DifficultyLevel.BEGINNER;
    }

    public boolean isShortDuration() {
        return durationMinutes != null && durationMinutes <= 10;
    }

    public boolean isMediumDuration() {
        return durationMinutes != null && durationMinutes > 10 && durationMinutes <= 30;
    }

    public boolean isLongDuration() {
        return durationMinutes != null && durationMinutes > 30;
    }
}