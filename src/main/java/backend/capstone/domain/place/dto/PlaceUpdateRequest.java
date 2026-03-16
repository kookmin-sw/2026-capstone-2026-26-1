package backend.capstone.domain.place.dto;

public record PlaceUpdateRequest(
    String roadAddress,
    String placeName
) {

}
