package backend.capstone.domain.care.caredependent.sse.registry;

import backend.capstone.domain.care.caredependent.sse.dto.CareSseEventType;
import backend.capstone.domain.care.caredependent.sse.dto.CareSseMessagePayload;
import backend.capstone.domain.care.caredependent.sse.service.DependentWatchService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class CareSseEmitterRegistry {

    private static final long SSE_TIMEOUT_MILLIS = 60L * 60L * 1000L; //1시간

    private final Map<Long, Map<String, SseEmitter>> emittersByGuardianUserId =
        new ConcurrentHashMap<>();

    private final DependentWatchService dependentWatchService;

    public SseEmitter register(Long guardianUserId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        String emitterId = UUID.randomUUID().toString();

        emittersByGuardianUserId.computeIfAbsent(guardianUserId,
            ignored -> new ConcurrentHashMap<>()).put(emitterId, emitter);

        emitter.onCompletion(() -> remove(guardianUserId, emitterId));
        emitter.onTimeout(() -> remove(guardianUserId, emitterId));
        emitter.onError(ignored -> remove(guardianUserId, emitterId));

        dependentWatchService.startWatching(guardianUserId);
        sendConnectedEvent(guardianUserId, emitterId, emitter);
        return emitter;
    }

    public List<SseEmitter> getEmitters(Long guardianUserId) {
        Map<String, SseEmitter> emitters = emittersByGuardianUserId.get(guardianUserId);
        if (emitters == null || emitters.isEmpty()) {
            return List.of();
        }
        return List.copyOf(emitters.values());
    }

    public void publish(Long guardianUserId, CareSseEventType eventType, Object data) {
        Map<String, SseEmitter> emitters = emittersByGuardianUserId.get(guardianUserId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            sendEvent(guardianUserId, entry.getKey(), entry.getValue(),
                SseEmitter.event().name(eventType.getEventName()).data(data));
        }
    }

    public void publishHeartbeat() {
        List<Long> guardianUserIds = List.copyOf(emittersByGuardianUserId.keySet());
        CareSseMessagePayload payload = CareSseMessagePayload.of("keep-alive");
        for (Long guardianUserId : guardianUserIds) {
            publish(guardianUserId, CareSseEventType.HEARTBEAT, payload);
        }
    }

    public void remove(Long guardianUserId, String emitterId) {
        Map<String, SseEmitter> emitters = emittersByGuardianUserId.get(guardianUserId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitterId);
        if (emitters.isEmpty()) {
            emittersByGuardianUserId.remove(guardianUserId);
            dependentWatchService.stopWatching(guardianUserId);
        }
    }

    public int count(Long guardianUserId) {
        Map<String, SseEmitter> emitters = emittersByGuardianUserId.get(guardianUserId);
        return emitters == null ? 0 : emitters.size();
    }

    private void sendConnectedEvent(Long guardianUserId, String emitterId, SseEmitter emitter) {
        sendEvent(guardianUserId, emitterId, emitter, SseEmitter.event()
            .name(CareSseEventType.CONNECTED.getEventName())
            .data(CareSseMessagePayload.of("보호 대상 위치 SSE 연결이 생성되었습니다.")));
    }

    private void sendEvent(Long guardianUserId, String emitterId, SseEmitter emitter,
        SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException e) {
            log.warn("SSE 이벤트 전송에 실패했습니다. guardianUserId={}, emitterId={}",
                guardianUserId, emitterId, e);
            remove(guardianUserId, emitterId);
        }
    }
}
