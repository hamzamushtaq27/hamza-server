package com.dgsw.hamza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private TreatmentProgram program;

    @Column(name = "completed_sessions")
    @Builder.Default
    private Integer completedSessions = 0;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "progress_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressPercentage = BigDecimal.ZERO;

    @Column(name = "last_session_date")
    private LocalDateTime lastSessionDate;

    @Column(name = "started_date")
    private LocalDateTime startedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "notes")
    private String notes;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    @Builder.Default
    private Integer longestStreak = 0;

    @Column(name = "total_time_spent")
    @Builder.Default
    private Integer totalTimeSpent = 0; // in minutes

    // Convenience methods
    public boolean isCompleted() {
        return Boolean.TRUE.equals(isCompleted);
    }

    public void incrementSession() {
        this.completedSessions++;
        this.lastSessionDate = LocalDateTime.now();
        updateProgressPercentage();
        updateStreak();
    }

    private void updateProgressPercentage() {
        if (totalSessions != null && totalSessions > 0) {
            double percentage = (double) completedSessions / totalSessions * 100;
            this.progressPercentage = BigDecimal.valueOf(Math.min(percentage, 100));
        }
    }

    private void updateStreak() {
        // Simple streak logic - increment if session is on consecutive days
        if (lastSessionDate != null) {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            if (lastSessionDate.toLocalDate().equals(yesterday.toLocalDate())) {
                this.currentStreak++;
            } else {
                this.currentStreak = 1;
            }
            
            if (this.currentStreak > this.longestStreak) {
                this.longestStreak = this.currentStreak;
            }
        }
    }

    public void completeProgress() {
        this.isCompleted = true;
        this.completedDate = LocalDateTime.now();
        this.progressPercentage = BigDecimal.valueOf(100);
    }

    public boolean isJustStarted() {
        return completedSessions <= 1;
    }

    public boolean isHalfway() {
        return progressPercentage.compareTo(BigDecimal.valueOf(50)) >= 0;
    }

    public boolean isNearlyComplete() {
        return progressPercentage.compareTo(BigDecimal.valueOf(80)) >= 0;
    }

    public void addTimeSpent(int minutes) {
        this.totalTimeSpent += minutes;
    }
}