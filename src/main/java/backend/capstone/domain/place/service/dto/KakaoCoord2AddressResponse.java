package backend.capstone.domain.place.service.dto;

import java.util.List;

public record KakaoCoord2AddressResponse(
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
        String building_name
    ) {

    }

    public record Address(
        String address_name
    ) {

    }

}
