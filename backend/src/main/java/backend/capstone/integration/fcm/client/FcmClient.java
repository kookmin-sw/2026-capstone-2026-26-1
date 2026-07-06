package backend.capstone.integration.fcm.client;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class FcmClient {

    private static final String DATA_KEY_TYPE = "type";
    private static final String DATA_KEY_WATCHING = "watching";
    private static final String DATA_TYPE_TRACKING_INTERVAL_CHANGE = "TRACKING_INTERVAL_CHANGE";

    private final FirebaseMessaging firebaseMessaging;

    public FcmClient(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public void sendWatchingStatusChanged(String fcmToken, boolean watching) {
        if (!StringUtils.hasText(fcmToken)) {
            return;
        }

        Message message = Message.builder()
            .setToken(fcmToken)
            .putData(DATA_KEY_TYPE, DATA_TYPE_TRACKING_INTERVAL_CHANGE)
            .putData(DATA_KEY_WATCHING, String.valueOf(watching))
            .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("위치 조회 상태 변경 FCM 발송에 실패했습니다. watching={}", watching, e);
        }
    }
}
