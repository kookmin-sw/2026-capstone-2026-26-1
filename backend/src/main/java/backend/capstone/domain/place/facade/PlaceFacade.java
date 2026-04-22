package backend.capstone.domain.place.facade;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceListResponse;
import backend.capstone.domain.place.dto.PlaceReorderRequest;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.mapper.PlaceMapper;
import backend.capstone.domain.place.service.PlaceService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceFacade {

    private final DayRouteService dayRouteService;
    private final PlaceService placeService;

    @Transactional(readOnly = true)
    public PlaceListResponse getPlaces(LocalDate date, Long userId) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);
        List<Place> places = placeService.getPlacesByDayRoute(dayRoute);
        return PlaceMapper.toPlaceListResponse(places);
    }

    @Transactional
    public PlaceAddResponse addPlace(LocalDate date, Long userId, PlaceAddRequest request) {
        DayRoute dayRoute = dayRouteService.getOrCreate(userId, date);
        return placeService.addPlace(dayRoute, request);
    }

    @Transactional
    public PlaceUpdateResponse updatePlace(LocalDate date, Long userId,
        Long placeId, PlaceUpdateRequest request) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);
        return placeService.updatePlace(dayRoute, placeId, request);
    }

    @Transactional
    public void deletePlace(LocalDate date, Long userId, Long placeId) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);
        placeService.deletePlace(dayRoute, placeId);
    }

    @Transactional
    public void reorderPlace(LocalDate date, Long userId, PlaceReorderRequest request) {
        DayRoute dayRoute = dayRouteService.getDayRouteByDateAndUserId(date, userId);
        placeService.reorderPlace(dayRoute, request);
    }
}
