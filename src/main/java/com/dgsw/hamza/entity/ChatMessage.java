package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.CrisisLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_session_id", nullable = false)
    private ChatSession chatSession;

    @Column(name = "message_content", length = 2000)
    private String messageContent;

    @Column(name = "is_from_user")
    @Builder.Default
    private Boolean isFromUser = true;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "message_type")
    @Builder.Default
    private String messageType = "TEXT"; // TEXT, IMAGE, AUDIO, SYSTEM

    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1 to 1 scale

    @Column(name = "emotion_score")
    private Double emotionScore;

    @Column(name = "crisis_keywords")
    private String crisisKeywords;

    @Column(name = "is_crisis_related")
    @Builder.Default
    private Boolean isCrisisRelated = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_level")
    private CrisisLevel crisisLevel;

    @Column(name = "ai_response_confidence")
    private Double aiResponseConfidence;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    // Convenience methods
    public boolean isFromUser() {
        return Boolean.TRUE.equals(isFromUser);
    }

    public boolean isFromBot() {
        return !isFromUser();
    }

    public boolean isRead() {
        return Boolean.TRUE.equals(isRead);
    }

    public boolean isCrisisRelated() {
        return Boolean.TRUE.equals(isCrisisRelated);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsCrisisRelated(String keywords) {
        this.isCrisisRelated = true;
        this.crisisKeywords = keywords;
    }

    public boolean isTextMessage() {
        return "TEXT".equals(messageType);
    }

    public boolean isImageMessage() {
        return "IMAGE".equals(messageType);
    }

    public boolean isAudioMessage() {
        return "AUDIO".equals(messageType);
    }

    public boolean isSystemMessage() {
        return "SYSTEM".equals(messageType);
    }

    public boolean hasPositiveSentiment() {
        return sentimentScore != null && sentimentScore > 0.3;
    }

    public boolean hasNegativeSentiment() {
        return sentimentScore != null && sentimentScore < -0.3;
    }

    public boolean hasHighConfidence() {
        return aiResponseConfidence != null && aiResponseConfidence > 0.8;
    }

    public boolean isQuickResponse() {
        return processingTimeMs != null && processingTimeMs < 1000;
    }
}