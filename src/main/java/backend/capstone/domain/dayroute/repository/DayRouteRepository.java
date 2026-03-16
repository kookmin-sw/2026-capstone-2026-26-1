package backend.capstone.domain.dayroute.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DayRouteRepository extends JpaRepository<DayRoute, Long> {

    @Query("""
            select dr
            from DayRoute dr
            where dr.user.id = :userId
              and dr.date = :date
        """)
    Optional<DayRoute> findByUserIdAndDate(@Param("userId") Long userId,
        @Param("date") LocalDate date);
}
