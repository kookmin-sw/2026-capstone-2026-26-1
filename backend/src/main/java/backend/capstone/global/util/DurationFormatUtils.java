package backend.capstone.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DurationFormatUtils {

    public static String formatOutingDurationText(long totalOutingSeconds) {
        long totalMinutes = totalOutingSeconds / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return minutes > 0 ? hours + "시간 " + minutes + "분" : hours + "시간";
        }

        return minutes + "분";
    }
}
