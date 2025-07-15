package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.RecommendationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @Column(name = "recommended_date", nullable = false)
    private LocalDateTime recommendedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.PENDING;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5; // 1 (highest) to 10 (lowest)

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "notes")
    private String notes;

    @Column(name = "started_date")
    private LocalDateTime startedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    // Convenience methods
    public boolean isPending() {
        return status == RecommendationStatus.PENDING;
    }

    public boolean isActive() {
        return status == RecommendationStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == RecommendationStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == RecommendationStatus.CANCELLED;
    }

    public boolean isPaused() {
        return status == RecommendationStatus.PAUSED;
    }

    public boolean isHighPriority() {
        return priority != null && priority <= 3;
    }

    public boolean isLowPriority() {
        return priority != null && priority >= 7;
    }

    public void activate() {
        this.status = RecommendationStatus.ACTIVE;
        this.startedDate = LocalDateTime.now();
    }

    public void complete() {
        this.status = RecommendationStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = RecommendationStatus.CANCELLED;
        this.cancelledDate = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void pause() {
        this.status = RecommendationStatus.PAUSED;
    }
}