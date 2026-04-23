package backend.capstone.domain.place.service;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.bookmarkplace.service.BookmarkPlaceService;
import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.dayroute.service.DayRouteService;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.ongoingstay.service.dto.PlaceSearchResult;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceReorderRequest;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.exception.PlaceErrorCode;
import backend.capstone.domain.place.mapper.PlaceMapper;
import backend.capstone.domain.place.repository.PlaceRepository;
import backend.capstone.global.exception.BusinessException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final DayRouteService dayRouteService;
    private final PlaceRepository placeRepository;
    private final BookmarkPlaceService bookmarkPlaceService;

    @Transactional
    public PlaceAddResponse addPlace(DayRoute dayRoute, PlaceAddRequest request) {
        int newOrder = getNewOrder(dayRoute);

        Place place = PlaceMapper.toEntityByManual(dayRoute, request, newOrder);
        applyBookmarkPlaceType(dayRoute, place);

        Place savedPlace = placeRepository.save(place);
        dayRouteService.refreshHasManualData(dayRoute);

        return PlaceMapper.toPlaceAddResponse(savedPlace);
    }

    @Transactional(readOnly = true)
    public List<Place> getPlacesByDayRoute(DayRoute dayRoute) {
        return placeRepository.findByDayRouteOrderByOrderIndex(dayRoute);
    }

    @Transactional
    public PlaceUpdateResponse updatePlace(DayRoute dayRoute, Long placeId,
        PlaceUpdateRequest request) {
        Place place = placeRepository.findByIdAndDayRoute(placeId, dayRoute)
            .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));

        place.update(request.roadAddress(), request.placeName(), request.latitude(),
            request.longitude());
        applyBookmarkPlaceType(dayRoute, place);

        return PlaceMapper.toPlaceUpdateResponse(place);
    }

    @Transactional
    public void deletePlace(DayRoute dayRoute, Long placeId) {
        Place place = placeRepository.findByIdAndDayRoute(placeId, dayRoute)
            .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));

        int deletedOrderIdx = place.getOrderIndex();
        placeRepository.delete(place);
        placeRepository.decrementOrderIndexesGreaterThan(dayRoute, deletedOrderIdx);
        dayRouteService.refreshHasManualData(dayRoute);
    }

    @Transactional
    public void reorderPlace(DayRoute dayRoute, PlaceReorderRequest request) {
        List<Place> places = placeRepository.findByDayRouteOrderByOrderIndex(dayRoute);
        List<Long> reorderedPlaceIds = request.placeIds();

        //개수 검사
        if (places.size() != reorderedPlaceIds.size()) {
            throw new BusinessException(PlaceErrorCode.INVALID_PLACE_REORDER);
        }

        //중복 검사
        Set<Long> uniqueIds = Set.copyOf(reorderedPlaceIds);
        if (uniqueIds.size() != reorderedPlaceIds.size()) {
            throw new BusinessException(PlaceErrorCode.INVALID_PLACE_REORDER);
        }

        Map<Long, Place> placeMap = new HashMap<>();
        for (Place place : places) {
            placeMap.put(place.getId(), place);
        }

        for (Long placeId : reorderedPlaceIds) {
            if (!placeMap.containsKey(placeId)) {
                throw new BusinessException(PlaceErrorCode.INVALID_PLACE_REORDER);
            }
        }

        for (int i = 0; i < places.size(); i++) {
            places.get(i).changeOrderIndex(-(i + 1));
        }

        placeRepository.flush();

        for (int i = 0; i < reorderedPlaceIds.size(); i++) {
            Place place = placeMap.get(reorderedPlaceIds.get(i));
            place.changeOrderIndex(i + 1);
        }
    }

    @Transactional
    public void saveAutoPlace(
        DayRoute dayRoute,
        OngoingStay stay,
        Optional<PlaceSearchResult> searchResult
    ) {
        int newOrder = getNewOrder(dayRoute);

        Place place = searchResult
            .map(result -> PlaceMapper.toEntityByAuto(dayRoute, result, newOrder,
                stay.getStartTime(), stay.getLastTime()))
            .orElseGet(
                () -> PlaceMapper.toUnknownAuto(dayRoute, stay.getCenterLatitude(), stay.getCenterLongitude(), newOrder,
                    stay.getStartTime(), stay.getLastTime()));

        applyBookmarkPlaceType(dayRoute, place);
        placeRepository.save(place);
    }

    private void applyBookmarkPlaceType(DayRoute dayRoute, Place place) {
        String roadAddress = emptyToNull(place.getRoadAddress());
        place.changeType(
            roadAddress == null ? null
                : bookmarkPlaceService.getBookmarkPlaceByUserId(dayRoute.getUser().getId()).stream()
                    .filter(bookmarkPlace -> roadAddress.equals(bookmarkPlace.getRoadAddress()))
                    .findFirst()
                    .map(BookmarkPlace::getType)
                    .orElse(null)
        );
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private int getNewOrder(DayRoute dayRoute) {
        int maxOrder = placeRepository.findMaxOrderIdxByRoute(dayRoute);
        return maxOrder + 1;
    }
}
