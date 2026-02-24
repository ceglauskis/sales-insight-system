package com.salesinsight.infra.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AssemblyAITranscriptionService implements TranscriptionService {

    private final RestClient restClient;
    private final String apiKey;
    private final String uploadUrl;
    private final String transcriptUrl;

    public AssemblyAITranscriptionService(
            @Value("${ai.assemblyai.api-key}") String apiKey,
            @Value("${ai.assemblyai.upload-url}") String uploadUrl,
            @Value("${ai.assemblyai.transcript-url}") String transcriptUrl
    ) {
        this.apiKey = apiKey;
        this.uploadUrl = uploadUrl;
        this.transcriptUrl = transcriptUrl;
        this.restClient = RestClient.create();
    }

    @Override
    public String transcribe(String filePath) {
        log.info("Iniciando transcrição. filePath={}", filePath);

        String fileUrl = uploadFile(filePath);
        log.debug("Arquivo enviado ao AssemblyAI. fileUrl={}", fileUrl);

        String transcriptId = requestTranscription(fileUrl);
        log.debug("Transcrição solicitada. transcriptId={}", transcriptId);

        String transcription = pollForResult(transcriptId);
        log.info("Transcrição concluída. transcriptId={}", transcriptId);

        return transcription;
    }

    private String uploadFile(String filePath) {
        var response = restClient.post()
                .uri(uploadUrl)
                .header("Authorization", apiKey)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(filePath))
                .retrieve()
                .body(Map.class);

        return (String) response.get("upload_url");
    }

    private String requestTranscription(String fileUrl) {
        var response = restClient.post()
                .uri(transcriptUrl)
                .header("Authorization", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "audio_url", fileUrl,
                        "speech_models", List.of("universal-2")
                ))
                .retrieve()
                .body(Map.class);

        return (String) response.get("id");
    }

    private String pollForResult(String transcriptId) {
        String url = transcriptUrl + "/" + transcriptId;

        while (true) {
            var response = restClient.get()
                    .uri(url)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .body(Map.class);

            String status = (String) response.get("status");
            log.debug("Status da transcrição: {}", status);

            switch (status) {
                case "completed" -> { return (String) response.get("text"); }
                case "error" -> throw new RuntimeException("Erro na transcrição: " + response.get("error"));
                default -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Polling interrompido", e);
                    }
                }
            }
        }
    }
}