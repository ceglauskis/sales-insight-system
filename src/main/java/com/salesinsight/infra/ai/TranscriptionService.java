package com.salesinsight.infra.ai;

import java.util.UUID;

public interface TranscriptionService {
    String transcribe(String filePath);
}
