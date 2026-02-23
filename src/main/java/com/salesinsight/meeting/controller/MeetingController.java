package com.salesinsight.meeting.controller;

import com.salesinsight.infra.security.AuthenticatedUser;
import com.salesinsight.meeting.dto.MeetingResponse;
import com.salesinsight.meeting.dto.MeetingUploadRequest;
import com.salesinsight.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<MeetingResponse> upload(
            @Valid MeetingUploadRequest request,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        MeetingResponse response = meetingService.upload(request, authenticatedUser.userId());
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<MeetingResponse> findById(
            @PathVariable UUID meetingId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        MeetingResponse response = meetingService.findById(meetingId, authenticatedUser.userId());
        return ResponseEntity.ok(response);
    }
}
