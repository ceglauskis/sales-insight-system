package com.salesinsight.meeting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record MeetingUploadRequest(

        @NotBlank(message = "Título é obrigatório")
        String title,

        @NotNull(message = "Arquivo é obrigatório")
        MultipartFile file
) {

}
