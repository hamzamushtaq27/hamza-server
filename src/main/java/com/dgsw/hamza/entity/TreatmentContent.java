package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.ContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "treatment_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreatmentContent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    private ContentType contentType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "transcript")
    private String transcript;

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isAudioContent() {
        return contentType == ContentType.AUDIO;
    }

    public boolean isVideoContent() {
        return contentType == ContentType.VIDEO;
    }

    public boolean isTextContent() {
        return contentType == ContentType.TEXT;
    }

    public boolean hasFile() {
        return fileUrl != null && !fileUrl.trim().isEmpty();
    }
}