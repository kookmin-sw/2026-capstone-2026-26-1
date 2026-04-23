package backend.capstone.domain.kakaoplace.mapper;

import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse;
import backend.capstone.domain.kakaoplace.dto.PlaceSearchResponse.PlaceSearchItem;
import backend.capstone.domain.kakaoplace.service.dto.KakaoLocalSearchResult;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class KakaoPlaceMapper {

    public static PlaceSearchResponse toPlaceSearchResponse(
        KakaoLocalSearchResult kakaoLocalSearchResult
    ) {
        List<PlaceSearchItem> items = kakaoLocalSearchResult.documents() == null
            ? List.of()
            : kakaoLocalSearchResult.documents().stream()
                .map(document -> new PlaceSearchItem(
                    document.place_name(),
                    document.category_name(),
                    document.road_address_name(),
                    parseDouble(document.x()),
                    parseDouble(document.y())
                ))
                .toList();

        return new PlaceSearchResponse(items.size(), items);
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            double parsed = Double.parseDouble(value);
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
