package backend.capstone.domain.dayroute.mapper;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.user.entity.User;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DayRouteMapper {

    public static DayRoute toEntity(User user, LocalDate date) {
        return DayRoute.builder()
            .user(user)
            .date(date)
            .build();
    }
}
