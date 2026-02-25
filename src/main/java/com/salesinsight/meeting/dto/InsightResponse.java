package com.salesinsight.meeting.dto;

import com.salesinsight.meeting.domain.Insight;
import com.salesinsight.meeting.domain.Sentiment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InsightResponse(
        UUID id,
        String summary,
        Sentiment sentiment,
        List<String> actionPoints,
        List<String> nextSteps,
        LocalDateTime generatedAt
) {
    public static InsightResponse from(Insight insight) {
        return new InsightResponse(
                insight.getId(),
                insight.getSummary(),
                insight.getSentiment(),
                insight.getActionPoints(),
                insight.getNextSteps(),
                insight.getGeneratedAt()
        );
    }
}