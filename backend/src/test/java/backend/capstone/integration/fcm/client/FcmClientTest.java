package backend.capstone.integration.fcm.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmClientTest {

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @Test
    void fcm토큰이_없으면_발송하지_않는다() throws FirebaseMessagingException {
        FcmClient fcmClient = new FcmClient(firebaseMessaging);

        fcmClient.sendWatchingStatusChanged(null, true);
        fcmClient.sendWatchingStatusChanged("   ", true);

        then(firebaseMessaging).should(never()).send(any(Message.class));
    }

    @Test
    void fcm토큰이_있으면_발송한다() throws FirebaseMessagingException {
        FcmClient fcmClient = new FcmClient(firebaseMessaging);

        fcmClient.sendWatchingStatusChanged("fcm-token", true);

        then(firebaseMessaging).should().send(any(Message.class));
    }

    @Test
    void 발송_실패시_예외를_전파하지_않는다() throws FirebaseMessagingException {
        FcmClient fcmClient = new FcmClient(firebaseMessaging);
        given(firebaseMessaging.send(any(Message.class)))
            .willThrow(mock(FirebaseMessagingException.class));

        fcmClient.sendWatchingStatusChanged("fcm-token", true);
    }
}
