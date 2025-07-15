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
@Table(name = "diagnosis_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisQuestion extends BaseEntity {

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    // Relationships
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DiagnosisAnswer> answers = new ArrayList<>();

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }
}