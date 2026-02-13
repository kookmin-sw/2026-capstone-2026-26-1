package backend.capstone.domain.dayroute.controller;

import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.service.DayRouteService;
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
@RequestMapping("/api/day-route")
public class DayRouteController {

    private DayRouteService dayRouteService;

    @PostMapping("/gps-points/upload")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal User user
    ) {
        return dayRouteService.uploadGpsPoint(user, request);
    }
}
