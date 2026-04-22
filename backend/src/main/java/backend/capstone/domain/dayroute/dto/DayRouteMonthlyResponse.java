package backend.capstone.domain.dayroute.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record DayRouteMonthlyResponse(
    int year,
    int month,
    List<DayItem> days
) {

    @Builder
    public record DayItem(
        LocalDate date,
        boolean dayRouteExists,
        DayRouteItem dayRoute
    ) {

    }

    @Builder
    public record DayRouteItem(
        boolean hasPolyline,
        boolean hasDetails,
        boolean isBookmarked
    ) {

    }
}
