package backend.capstone.domain.ongoinghomestatus.service;

import backend.capstone.domain.bookmarkplace.entity.BookmarkPlace;
import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.ongoinghomestatus.entity.HomeZoneStatus;
import backend.capstone.global.util.GeoUtils;
import org.springframework.stereotype.Component;

@Component
public class HomeZoneDecider {

    private static final int INITIAL_HOME_RADIUS_METER = 100;
    private static final int ENTER_HOME_RADIUS_METER = 80;
    private static final int EXIT_HOME_RADIUS_METER = 120;

    public HomeZoneStatus determineInitialZone(GpsPoint point, BookmarkPlace homeBookmark) {
        double distance = distanceToHome(point, homeBookmark);

        if (distance <= INITIAL_HOME_RADIUS_METER) {
            return HomeZoneStatus.IN_HOME;
        }

        return HomeZoneStatus.OUT_HOME;
    }

    public HomeZoneStatus determineObservedZone(GpsPoint point, BookmarkPlace homeBookmark,
        HomeZoneStatus currentZoneStatus) {
        double distance = distanceToHome(point, homeBookmark);

        if (currentZoneStatus == HomeZoneStatus.IN_HOME) {
            if (distance < EXIT_HOME_RADIUS_METER) {
                return HomeZoneStatus.IN_HOME;
            }

            return HomeZoneStatus.OUT_HOME;
        }

        if (distance <= ENTER_HOME_RADIUS_METER) {
            return HomeZoneStatus.IN_HOME;
        }

        return HomeZoneStatus.OUT_HOME;
    }

    private double distanceToHome(GpsPoint point, BookmarkPlace homeBookmark) {
        return GeoUtils.distanceMeter(point.getLatitude(), point.getLongitude(),
            homeBookmark.getLatitude(), homeBookmark.getLongitude());
    }
}
