package com.salesinsight.meeting.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MeetingTest {

    private Meeting meeting;

    @BeforeEach
    void setUp() {
        meeting = new Meeting("Reunião de Vendas", "uploads/audio.mp3", UUID.randomUUID());
    }

    @Test
    @DisplayName("Meeting deve iniciar com status CREATED")
    void shouldStartWithStatusCreated() {
        assertEquals(MeetingStatus.CREATED, meeting.getStatus());
    }

    @Test
    @DisplayName("Deve ir para PROCESSING a partir de CREATED")
    void shouldTransitionToProcessing() {
        meeting.markAsProcessing();
        assertEquals(MeetingStatus.PROCESSING, meeting.getStatus());
    }

    @Test
    @DisplayName("Não deve ir para PROCESSING se já estiver PROCESSING")
    void shouldNotTransitionToProcessingIfAlreadyProcessing() {
        meeting.markAsProcessing();
        assertThrows(IllegalStateException.class, () -> meeting.markAsProcessing());
    }

    @Test
    @DisplayName("Deve ir para PROCESSED a partir de PROCESSING")
    void shouldTransitionToProcessed() {
        meeting.markAsProcessing();
        meeting.markAsProcessed();
        assertEquals(MeetingStatus.PROCESSED, meeting.getStatus());
    }

    @Test
    @DisplayName("Não deve ir para PROCESSED a partir de CREATED")
    void shouldNotTransitionToProcessedFromCreated() {
        assertThrows(IllegalStateException.class, () -> meeting.markAsProcessed());
    }

    @Test
    @DisplayName("Deve ir para FAILED a partir de qualquer status")
    void shouldTransitionToFailedFromAnyStatus() {
        meeting.markAsFailed();
        assertEquals(MeetingStatus.FAILED, meeting.getStatus());
    }

    @Test
    @DisplayName("Deve adicionar insight quando status é PROCESSING")
    void shouldAddInsightWhenProcessing() {
        meeting.markAsProcessing();
        Insight insight = new Insight(meeting, "Resumo", Sentiment.POSITIVE,
                java.util.List.of("Ponto 1"), java.util.List.of("Próximo passo 1"));
        meeting.addInsight(insight);
        assertEquals(1, meeting.getInsights().size());
    }

    @Test
    @DisplayName("Não deve adicionar insight quando status não é PROCESSING")
    void shouldNotAddInsightWhenNotProcessing() {
        Insight insight = new Insight(meeting, "Resumo", Sentiment.POSITIVE,
                java.util.List.of("Ponto 1"), java.util.List.of("Próximo passo 1"));
        assertThrows(IllegalStateException.class, () -> meeting.addInsight(insight));
    }

    @Test
    @DisplayName("Não deve salvar transcrição sem videoUrl")
    void shouldNotSaveTranscriptionWithoutVideoUrl() {
        Meeting meetingWithoutVideo = new Meeting("Título", null, UUID.randomUUID());
        assertThrows(IllegalStateException.class,
                () -> meetingWithoutVideo.saveTranscription("transcrição"));
    }
}
