package com.dgsw.hamza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diagnosis_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private DiagnosisQuestion question;

    @Column(name = "answer_score", nullable = false)
    private Integer answerScore;

    @Column(name = "answer_text")
    private String answerText;

    // Convenience methods
    public boolean isHighScore() {
        return answerScore >= 3; // PHQ-9 high score threshold
    }

    public boolean isLowScore() {
        return answerScore <= 1; // PHQ-9 low score threshold
    }
}