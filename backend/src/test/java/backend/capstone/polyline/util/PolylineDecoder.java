package backend.capstone.polyline.util;

import java.util.ArrayList;
import java.util.List;

public class PolylineDecoder {

    private static final double SCALE = 1e5;

    public static List<Point> decode(String encoded) {
        List<Point> points = new ArrayList<>();

        int index = 0;
        long lat = 0;
        long lng = 0;

        while (index < encoded.length()) {
            long[] latResult = decodeNext(encoded, index);
            lat += latResult[0];
            index = (int) latResult[1];

            long[] lngResult = decodeNext(encoded, index);
            lng += lngResult[0];
            index = (int) lngResult[1];

            points.add(new Point(lat / SCALE, lng / SCALE));
        }

        return points;
    }

    /**
     * return[0] = decoded delta return[1] = next index
     */
    private static long[] decodeNext(String encoded, int startIndex) {
        long result = 0;
        int shift = 0;
        int index = startIndex;
        int b;

        do {
            b = encoded.charAt(index++) - 63;
            result |= (long) (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);

        long delta = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
        return new long[]{delta, index};
    }

    public record Point(double latitude, double longitude) {

    }
}
