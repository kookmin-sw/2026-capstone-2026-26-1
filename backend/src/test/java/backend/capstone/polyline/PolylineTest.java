package backend.capstone.polyline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import backend.capstone.domain.gpspoint.entity.GpsPoint;
import backend.capstone.domain.gpspoint.util.PolylineUtil;
import backend.capstone.polyline.util.PolylineDecoder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PolylineTest {

    private static final double TOLERANCE = 0.00001; // 1e-5

    @Test
    @DisplayName("raw 좌표들을 encode 후 decode 하면 거의 동일한 좌표로 복원된다")
    void encodeDecodeFivePoints() {
        List<GpsPoint> rawPoints = List.of(
            mockGpsPoint(37.566550, 126.978120),
            mockGpsPoint(14.23455, 43.2345),
            mockGpsPoint(10.234556, 320.50923),
            mockGpsPoint(53.23467, 202.4563)
        );

        String encoded = PolylineUtil.encode(rawPoints);
        List<PolylineDecoder.Point> decoded = PolylineDecoder.decode(encoded);

        assertEquals(rawPoints.size(), decoded.size(), "좌표 개수가 같아야 한다.");

        for (int i = 0; i < rawPoints.size(); i++) {
            GpsPoint raw = rawPoints.get(i);
            PolylineDecoder.Point restored = decoded.get(i);

            assertEquals(raw.getLatitude(), restored.latitude(), TOLERANCE,
                "latitude 불일치 at index = " + i);
            assertEquals(raw.getLongitude(), restored.longitude(), TOLERANCE,
                "longitude 불일치 at index = " + i);
        }
    }

    @Test
    @DisplayName("직선 형태의 많은 좌표도 encode/decode 후 거의 동일하게 복원된다")
    void encodeDecodeManyPoints() {
        List<GpsPoint> rawPoints = java.util.stream.IntStream.range(0, 1000)
            .mapToObj(i -> mockGpsPoint(
                37.50000 + (i * 0.00001),
                127.00000 + (i * 0.00002)
            ))
            .toList();

        String encoded = PolylineUtil.encode(rawPoints);
        List<PolylineDecoder.Point> decoded = PolylineDecoder.decode(encoded);

        assertEquals(rawPoints.size(), decoded.size(), "좌표 개수가 같아야 한다.");

        for (int i = 0; i < rawPoints.size(); i++) {
            GpsPoint raw = rawPoints.get(i);
            PolylineDecoder.Point restored = decoded.get(i);

            assertEquals(raw.getLatitude(), restored.latitude(), TOLERANCE,
                "latitude 불일치 at index = " + i);
            assertEquals(raw.getLongitude(), restored.longitude(), TOLERANCE,
                "longitude 불일치 at index = " + i);
        }
    }

    private GpsPoint mockGpsPoint(double latitude, double longitude) {
        GpsPoint point = Mockito.mock(GpsPoint.class);
        given(point.getLatitude()).willReturn(latitude);
        given(point.getLongitude()).willReturn(longitude);
        return point;
    }
}
