package backend.capstone.domain.timeline.gpspoint.repository;

import backend.capstone.domain.timeline.gpspoint.entity.GpsPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsPointRepository extends JpaRepository<GpsPoint, Long> {

}
