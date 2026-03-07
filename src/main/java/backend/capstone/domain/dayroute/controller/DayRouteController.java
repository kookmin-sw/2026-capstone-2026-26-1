package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.dto.GpsPointsResponse;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
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

    @PostMapping("/{date}/gps-points:batch")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @PathVariable LocalDate date,
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteService.uploadGpsPoint(date, principal.userId(), request);
    }

    @GetMapping("/{date}/gps-points")
    public GpsPointsResponse getGpsPoints(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteService.getGpsPoints(date, principal.userId());
    }

    @GetMapping("/{date}")
    public DayRouteDetailResponse getDayRouteDetail(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteService.getDayRouteDetail(date, principal.userId());
    }

    @PostMapping("/{date}/places")
    public PlaceAddResponse addPlaceToDayRoute(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody PlaceAddRequest request
    ) {
        return dayRouteService.addPlaceToDayRoute(date, principal.userId(), request);
    }

}
