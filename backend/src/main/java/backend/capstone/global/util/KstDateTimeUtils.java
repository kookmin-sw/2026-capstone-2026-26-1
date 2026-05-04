package backend.capstone.global.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KstDateTimeUtils {

    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    public static OffsetDateTime toKstOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(KST_ZONE_ID).toOffsetDateTime();
    }
}
