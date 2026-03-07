package backend.capstone.domain.place.service;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.place.dto.PlaceAddRequest;
import backend.capstone.domain.place.dto.PlaceAddResponse;
import backend.capstone.domain.place.dto.PlaceUpdateRequest;
import backend.capstone.domain.place.dto.PlaceUpdateResponse;
import backend.capstone.domain.place.entity.Place;
import backend.capstone.domain.place.exception.PlaceErrorCode;
import backend.capstone.domain.place.mapper.PlaceMapper;
import backend.capstone.domain.place.repository.PlaceRepository;
import backend.capstone.global.exception.BusinessException;
import java.util.List;
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
        return PlaceMapper.toPlaceUploadResponse(savedPlace);
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

        place.update(request.roadAddress(), request.placeName());

        return PlaceMapper.toPlaceUpdateResponse(place);
    }

}
