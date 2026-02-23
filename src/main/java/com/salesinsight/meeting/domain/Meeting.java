package com.salesinsight.meeting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meetings")
@Getter
@NoArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String transcription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Insight> insights = new ArrayList<>();

    public Meeting(String title, String videoUrl, UUID ownerId) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.ownerId = ownerId;
        this.status = MeetingStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsProcessing() {
        if (this.status != MeetingStatus.CREATED) {
            throw new IllegalStateException(
                    "Meeting só pode ir para PROCESSING a partir de CREATED. Status atual: " + this.status
            );
        }
        this.status = MeetingStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsProcessed() {
        if (this.status != MeetingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Meeting só pode ir para PROCESSED a partir de PROCESSING. Status atual: " + this.status
            );
        }
        this.status = MeetingStatus.PROCESSED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = MeetingStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public void addInsight(Insight insight) {
        if (this.status != MeetingStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Insight só pode ser adicionado enquanto Meeting está PROCESSING. Status atual: " + this.status
            );
        }
        this.insights.add(insight);
        this.updatedAt = LocalDateTime.now();
    }

    public void saveTranscription(String transcription) {
        if (this.videoUrl == null || this.videoUrl.isBlank()) {
            throw new IllegalStateException("Meeting não tem videoUrl, não pode ser transcrita.");
        }
        this.transcription = transcription;
        this.updatedAt = LocalDateTime.now();
    }

    public List<Insight> getInsights() {
        return Collections.unmodifiableList(insights);
    }
}
