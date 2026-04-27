package backend.capstone.domain.kakaoplace.service.dto;

import java.util.List;

public record KakaoSearchByCoordResult(
    Meta meta,
    List<Document> documents
) {

    public record Meta(
        Integer total_count
    ) {

    }

    public record Document(
        RoadAddress road_address,
        Address address
    ) {

    }

    public record RoadAddress(
        String address_name,
        String building_name,
        String x,
        String y
    ) {

    }

    public record Address(
        String address_name,
        String x,
        String y
    ) {

    }

}
