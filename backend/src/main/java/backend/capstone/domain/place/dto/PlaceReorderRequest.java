package backend.capstone.domain.place.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PlaceReorderRequest(
    @NotEmpty
    List<@NotNull @Positive Long> placeIds
) {

}
