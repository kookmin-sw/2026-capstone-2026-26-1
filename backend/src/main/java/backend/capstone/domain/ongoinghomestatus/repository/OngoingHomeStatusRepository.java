package backend.capstone.domain.ongoinghomestatus.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoinghomestatus.entity.OngoingHomeStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OngoingHomeStatusRepository extends JpaRepository<OngoingHomeStatus, Long> {

    Optional<OngoingHomeStatus> findByDayRoute(DayRoute dayRoute);
}
