package backend.capstone.domain.gpspoint.util;

import backend.capstone.domain.gpspoint.entity.GpsPoint;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolylineUtil {

    public static String encode(List<GpsPoint> points) {
        StringBuilder result = new StringBuilder();

        long lastLat = 0;
        long lastLng = 0;

        for (GpsPoint point : points) {
            long lat = Math.round(point.getLatitude() * 1e5);
            long lng = Math.round(point.getLongitude() * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encodeValue(dLat, result);
            encodeValue(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    private static void encodeValue(long value, StringBuilder result) {
        value = value < 0 ? ~(value << 1) : value << 1;

        while (value >= 0x20) {
            int nextValue = (int) ((0x20 | (value & 0x1f)) + 63);
            result.append((char) nextValue);
            value >>= 5;
        }

        value += 63;
        result.append((char) value);
    }
}
