package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.DiagnosisSeverity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diagnosis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiagnosisSeverity severity;

    @Column(name = "diagnosis_date", nullable = false)
    private LocalDateTime diagnosisDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    // Relationships
    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DiagnosisAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "diagnosis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();

    // Convenience methods
    public void calculateSeverity() {
        this.severity = DiagnosisSeverity.fromScore(this.totalScore);
    }

    public boolean isCompleted() {
        return Boolean.TRUE.equals(isCompleted);
    }

    public void complete() {
        this.isCompleted = true;
        this.diagnosisDate = LocalDateTime.now();
    }

    public boolean requiresImmediateAttention() {
        return severity == DiagnosisSeverity.SEVERE || severity == DiagnosisSeverity.VERY_SEVERE;
    }
}