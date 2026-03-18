package backend.capstone.domain.place.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceRepository placeRepository;

    @Transactional
    public PlaceAddResponse addPlace(DayRoute dayRoute, PlaceAddRequest request) {
        int maxOrder = placeRepository.findMaxOrderIdxByRoute(dayRoute);
        int newOrder = maxOrder + 1;

        Place savedPlace = placeRepository.save(PlaceMapper.toEntity(dayRoute, request, newOrder));
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

        return PlaceMapper.toPlaceUpdateResponse(place);
    }

    @Transactional
    public void deletePlace(DayRoute dayRoute, Long placeId) {
        Place place = placeRepository.findByIdAndDayRoute(placeId, dayRoute)
            .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));

        int deletedOrderIdx = place.getOrderIndex();
        placeRepository.delete(place);
        placeRepository.decrementOrderIndexesGreaterThan(dayRoute, deletedOrderIdx);
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

        // step 1. 임시 인덱스로 이동
        for (int i = 0; i < places.size(); i++) {
            places.get(i).changeOrderIndex(-(i + 1));
        }

        placeRepository.flush();

        // step 2. 최종 인덱스로 이동
        for (int i = 0; i < reorderedPlaceIds.size(); i++) {
            Place place = placeMap.get(reorderedPlaceIds.get(i));
            place.changeOrderIndex(i + 1);
        }

    }

}
