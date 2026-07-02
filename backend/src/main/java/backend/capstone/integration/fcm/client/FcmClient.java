package backend.capstone.integration.fcm.client;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmClient {

    private static final String DATA_KEY_TYPE = "type";
    private static final String DATA_KEY_INTERVAL_SECONDS = "intervalSeconds";
    private static final String DATA_TYPE_TRACKING_INTERVAL_CHANGE = "TRACKING_INTERVAL_CHANGE";

    private final FirebaseMessaging firebaseMessaging;

    public void sendTrackingIntervalChange(String fcmToken, int intervalSeconds) {
        if (!StringUtils.hasText(fcmToken)) {
            return;
        }

        Message message = Message.builder()
            .setToken(fcmToken)
            .putData(DATA_KEY_TYPE, DATA_TYPE_TRACKING_INTERVAL_CHANGE)
            .putData(DATA_KEY_INTERVAL_SECONDS, String.valueOf(intervalSeconds))
            .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("위치 전송 주기 변경 FCM 발송에 실패했습니다. intervalSeconds={}", intervalSeconds, e);
        }
    }
}
