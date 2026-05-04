package backend.capstone.domain.kakaoplace.service.dto;

import java.util.List;

public record KakaoSearchByCategoryResult(
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
        String category_group_code,
        String category_group_name,
        String phone,
        String address_name,
        String road_address_name,
        String x,
        String y,
        String place_url,
        String distance
    ) {

    }
}
