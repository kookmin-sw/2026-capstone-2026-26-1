package backend.capstone.domain.timeline.controller;

import backend.capstone.domain.timeline.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.timeline.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.timeline.gpspoint.service.GpsPointService;
import backend.capstone.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/timeline")
public class TimeLineController {

    private final GpsPointService gpsPointService;

    @PostMapping("/gps-points/batch-upload")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal User user
    ) {
        gpsPointService.batchInsert(user.getId(), request);
        return new GpsPointBatchUploadResponse("좌표 업로드에 성공했습니다.");
    }
}
