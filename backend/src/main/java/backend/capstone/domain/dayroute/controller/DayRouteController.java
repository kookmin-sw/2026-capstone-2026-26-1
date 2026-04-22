package backend.capstone.domain.dayroute.controller;

import backend.capstone.auth.dto.UserPrincipal;
import backend.capstone.domain.dayroute.dto.DayRouteDetailResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMemoRequest;
import backend.capstone.domain.dayroute.dto.DayRouteMemoResponse;
import backend.capstone.domain.dayroute.dto.DayRouteMonthlyResponse;
import backend.capstone.domain.dayroute.dto.DayRouteTitleRequest;
import backend.capstone.domain.dayroute.dto.DayRouteTitleResponse;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadRequest;
import backend.capstone.domain.dayroute.dto.GpsPointBatchUploadResponse;
import backend.capstone.domain.dayroute.facade.DayRouteFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/day-routes")
public class DayRouteController implements DayRouteControllerSpec {

    private final DayRouteFacade dayRouteFacade;

    @Override
    @PostMapping("/{date}/gps-points:batch")
    public GpsPointBatchUploadResponse uploadGpsPoints(
        @PathVariable LocalDate date,
        @Valid @RequestBody GpsPointBatchUploadRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteFacade.uploadGpsPoint(date, principal.userId(), request);
    }

    @Override
    @GetMapping
    public DayRouteMonthlyResponse getDayRoutesByMonth(
        @RequestParam @Min(2000) @Max(3000) int year,
        @RequestParam @Min(1) @Max(12) int month,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteFacade.getDayRoutesByMonth(year, month, principal.userId());
    }

    @Override
    @GetMapping("/{date}")
    public DayRouteDetailResponse getDayRouteDetail(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return dayRouteFacade.getDayRouteDetail(date, principal.userId());
    }

    @Override
    @PatchMapping("/{date}/memo")
    public DayRouteMemoResponse replaceMemo(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody DayRouteMemoRequest request
    ) {
        return dayRouteFacade.replaceMemo(date, principal.userId(), request);
    }

    @Override
    @PatchMapping("/{date}/title")
    public DayRouteTitleResponse replaceTitle(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestBody DayRouteTitleRequest request
    ) {
        return dayRouteFacade.replaceTitle(date, principal.userId(), request);
    }
}
