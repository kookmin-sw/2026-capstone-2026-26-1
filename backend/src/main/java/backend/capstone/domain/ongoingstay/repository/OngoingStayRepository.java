package backend.capstone.domain.ongoingstay.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OngoingStayRepository extends JpaRepository<OngoingStay, Long> {

    Optional<OngoingStay> findByDayRoute(DayRoute dayRoute);

    @Query("""
        select distinct os.dayRoute.id
        from OngoingStay os
        """)
    java.util.List<Long> findDayRouteIdsWithOngoingStay();
}
