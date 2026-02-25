package com.salesinsight.infra.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesinsight.meeting.domain.Insight;
import com.salesinsight.meeting.domain.Meeting;
import com.salesinsight.meeting.domain.Sentiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiInsightService implements InsightGeneratorService {

    private final RestClient restClient;
    private final String apiKey;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public GeminiInsightService(
            @Value("${ai.gemini.api-key}") String apiKey,
            @Value("${ai.gemini.url}") String apiUrl
    ) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Insight generate(Meeting meeting) {
        log.info("Gerando insights com Gemini. meetingId={}", meeting.getId());

        String prompt = buildPrompt(meeting.getTranscription());
        String rawResponse = callGemini(prompt);
        return parseResponse(meeting, rawResponse);
    }

    private String buildPrompt(String transcription) {
        return """
                Você é um assistente especializado em análise de calls de vendas.
                Analise a transcrição abaixo e responda APENAS com um JSON válido, sem markdown, sem explicações.
                
                Formato obrigatório:
                {
                  "summary": "resumo da reunião em 2-3 frases",
                  "sentiment": "POSITIVE" ou "NEUTRAL" ou "NEGATIVE",
                  "actionPoints": ["ponto 1", "ponto 2"],
                  "nextSteps": ["próximo passo 1", "próximo passo 2"]
                }
                
                Transcrição:
                """ + transcription;
    }

    private String callGemini(String prompt) {
        var body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        var response = restClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        log.debug("Resposta bruta do Gemini: {}", response);
        return response;
    }

    private Insight parseResponse(Meeting meeting, String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode json = objectMapper.readTree(text);

            String summary = json.path("summary").asText();
            Sentiment sentiment = Sentiment.valueOf(json.path("sentiment").asText());

            List<String> actionPoints = new ArrayList<>();
            json.path("actionPoints").forEach(node -> actionPoints.add(node.asText()));

            List<String> nextSteps = new ArrayList<>();
            json.path("nextSteps").forEach(node -> nextSteps.add(node.asText()));

            return new Insight(meeting, summary, sentiment, actionPoints, nextSteps);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao parsear resposta do Gemini: " + e.getMessage(), e);
        }
    }
}