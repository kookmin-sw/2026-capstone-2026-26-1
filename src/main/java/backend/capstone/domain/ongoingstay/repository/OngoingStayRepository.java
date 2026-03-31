package backend.capstone.domain.ongoingstay.repository;

import backend.capstone.domain.dayroute.entity.DayRoute;
import backend.capstone.domain.ongoingstay.entity.OngoingStay;
import backend.capstone.domain.ongoingstay.entity.OngoingStayStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OngoingStayRepository extends JpaRepository<OngoingStay, Long> {

    Optional<OngoingStay> findByDayRouteAndStatus(DayRoute dayRoute, OngoingStayStatus status);
}
