package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.ChatbotResponseType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chatbot_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponse extends BaseEntity {

    @Column(name = "trigger_keyword", nullable = false)
    private String triggerKeyword;

    @Column(name = "response_text", nullable = false, length = 2000)
    private String responseText;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", nullable = false)
    private ChatbotResponseType responseType;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 1; // 1 = highest priority

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "success_rate")
    @Builder.Default
    private Double successRate = 0.0;

    @Column(name = "context_tags")
    private String contextTags;

    @Column(name = "follow_up_action")
    private String followUpAction;

    @Column(name = "required_escalation")
    @Builder.Default
    private Boolean requiredEscalation = false;

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isHighPriority() {
        return priority <= 3;
    }

    public boolean isCrisisResponse() {
        return responseType == ChatbotResponseType.CRISIS;
    }

    public boolean requiresEscalation() {
        return Boolean.TRUE.equals(requiredEscalation);
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public void updateSuccessRate(boolean successful) {
        if (usageCount > 0) {
            double currentSuccessful = successRate * (usageCount - 1);
            if (successful) {
                currentSuccessful++;
            }
            this.successRate = currentSuccessful / usageCount;
        }
    }

    public boolean isEffectiveResponse() {
        return successRate > 0.7 && usageCount >= 10;
    }

    public boolean needsImprovement() {
        return successRate < 0.5 && usageCount >= 5;
    }
}