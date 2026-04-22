package backend.capstone.domain.place.dto;

public record PlaceAddRequest(
    String roadAddress,
    String placeName,
    double latitude,
    double longitude
) {

}
