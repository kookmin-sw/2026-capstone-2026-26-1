package backend.capstone.domain.place.service.dto;

import java.util.List;

public record KakaoTransCoordResponse(
    Meta meta,
    List<Document> documents
) {

    public record Meta(
        int total_count
    ) {
    }

    public record Document(
        Double x,
        Double y
    ) {
    }
}
