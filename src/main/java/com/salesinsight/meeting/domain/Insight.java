package com.salesinsight.meeting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "insights")
@Getter
@NoArgsConstructor
public class Insight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    @ElementCollection
    @CollectionTable(name = "insight_action_points", joinColumns = @JoinColumn(name = "insight_id"))
    @Column(name = "action_point")
    private List<String> actionPoints;

    @ElementCollection
    @CollectionTable(name = "insight_next_steps", joinColumns = @JoinColumn(name = "insight_id"))
    @Column(name = "next_step")
    private List<String> nextSteps;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    public Insight(Meeting meeting, String summary, Sentiment sentiment,
                   List<String> actionPoints, List<String> nextSteps) {
        this.meeting = meeting;
        this.summary = summary;
        this.sentiment = sentiment;
        this.actionPoints = actionPoints;
        this.nextSteps = nextSteps;
        this.generatedAt = LocalDateTime.now();
    }
}