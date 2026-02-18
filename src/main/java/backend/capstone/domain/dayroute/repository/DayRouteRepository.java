package backend.capstone.domain.dayroute.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.user.entity.User;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayRouteRepository extends JpaRepository<DayRoute, Long> {

    Optional<DayRoute> findByUserAndDate(User user, LocalDate date);

    @Query("""
            select distinct dr
            from DayRoute dr
            left join fetch dr.gpsPoints gp
            where dr.id = :dayRouteId
                and dr.user = :user
        """)
    Optional<DayRoute> findByIdAndUserWithGpsPoints(@Param("dayRouteId") Long DayRouteId,
        @Param("user") User user);
}
