package com.dgsw.hamza.entity;

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
@Table(name = "chat_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_name")
    private String sessionName;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "crisis_detected")
    @Builder.Default
    private Boolean crisisDetected = false;

    @Column(name = "crisis_level")
    private Integer crisisLevel; // 1-10 scale

    @Column(name = "total_messages")
    @Builder.Default
    private Integer totalMessages = 0;

    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1 to 1 scale

    @Column(name = "session_summary")
    private String sessionSummary;

    // Relationships
    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isCrisisDetected() {
        return Boolean.TRUE.equals(crisisDetected);
    }

    public void endSession() {
        this.isActive = false;
        this.endedAt = LocalDateTime.now();
    }

    public void detectCrisis(int level) {
        this.crisisDetected = true;
        this.crisisLevel = level;
    }

    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        this.totalMessages++;
    }

    public long getDurationMinutes() {
        if (startedAt == null) return 0;
        LocalDateTime end = endedAt != null ? endedAt : LocalDateTime.now();
        return java.time.Duration.between(startedAt, end).toMinutes();
    }

    public boolean isLongSession() {
        return getDurationMinutes() > 30;
    }

    public boolean isShortSession() {
        return getDurationMinutes() < 5;
    }

    public boolean hasHighCrisisLevel() {
        return crisisLevel != null && crisisLevel >= 8;
    }

    public boolean hasPositiveSentiment() {
        return sentimentScore != null && sentimentScore > 0.3;
    }

    public boolean hasNegativeSentiment() {
        return sentimentScore != null && sentimentScore < -0.3;
    }
}