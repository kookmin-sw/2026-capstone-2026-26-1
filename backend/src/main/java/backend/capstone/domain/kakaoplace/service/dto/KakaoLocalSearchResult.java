package backend.capstone.domain.kakaoplace.service.dto;

import java.util.List;

public record KakaoLocalSearchResult(
    Meta meta,
    List<Document> documents
) {

    public record Meta(
        int total_count,
        int pageable_count,
        boolean is_end
    ) {
    }

    public record Document(
        String id,
        String place_name,
        String category_name,
        String address_name,
        String road_address_name,
        String phone,
        String place_url,
        String x,
        String y
    ) {
    }
}
