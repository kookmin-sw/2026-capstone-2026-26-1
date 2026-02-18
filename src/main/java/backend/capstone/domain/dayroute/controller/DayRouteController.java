package backend.capstone.domain.dayroute.controller;

import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/day-routes")
public class DayRouteController {

    private final DayRouteService dayRouteService;

    @PostMapping("/gps-points/upload")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal User user
    ) {
        return dayRouteService.uploadGpsPoint(user, request);
    }

    @GetMapping("/{dayRouteId}/gps-points")
    public DayRouteDetailResponse getDayRouteDetail(
        @PathVariable Long dayRouteId,
        @AuthenticationPrincipal User user
    ) {
        return dayRouteService.getDayRouteDetail(dayRouteId, user);
    }
}
