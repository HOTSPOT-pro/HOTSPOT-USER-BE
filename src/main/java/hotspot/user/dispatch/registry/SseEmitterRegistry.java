package hotspot.user.dispatch.registry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRegistry {

    private final ConcurrentMap<Long, ConcurrentMap<String, SseEmitter>> emittersBySubId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SseEmitter> emittersByEmitterId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> subIdsByEmitterId = new ConcurrentHashMap<>();

    // 새 SSE 연결을 subId 기준으로 저장하고 emitterId를 발급한다.
    public String register(Long subId, SseEmitter emitter) {
        String emitterId = UUID.randomUUID().toString();
        emittersBySubId.computeIfAbsent(subId, key -> new ConcurrentHashMap<>()).put(emitterId, emitter);
        emittersByEmitterId.put(emitterId, emitter);
        subIdsByEmitterId.put(emitterId, subId);
        return emitterId;
    }

    // emitterId로 연결을 찾아 레지스트리에서 완전히 제거한다.
    public void remove(String emitterId) {
        emittersByEmitterId.remove(emitterId);
        Long subId = subIdsByEmitterId.remove(emitterId);
        if (subId == null) {
            return;
        }

        removeFromBucket(emittersBySubId, subId, emitterId);
    }

    // 특정 subId에 연결된 모든 SSE 연결을 강제로 종료하고 레지스트리에서 제거한다.
    public void closeAllBySubId(Long subId) {
        findBySubId(subId).forEach(registeredEmitter -> {
            remove(registeredEmitter.emitterId());
            try {
                registeredEmitter.emitter().complete();
            } catch (RuntimeException ignored) {
                // 연결 종료 중 예외가 발생해도 다른 emitter 정리는 계속 진행한다.
            }
        });
    }

    // 특정 subId에 연결된 모든 SSE 연결을 조회한다.
    public List<RegisteredEmitter> findBySubId(Long subId) {
        Map<String, SseEmitter> bucket = emittersBySubId.get(subId);
        if (bucket == null || bucket.isEmpty()) {
            return List.of();
        }

        return bucket.entrySet().stream()
                .map(entry -> new RegisteredEmitter(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 현재 등록된 전체 SSE 연결을 조회한다.
    public List<RegisteredEmitter> findAll() {
        return emittersByEmitterId.entrySet().stream()
                .map(entry -> new RegisteredEmitter(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 이벤트를 전송하고 실패하면 해당 연결을 제거, 종료한다.
    public void sendAndCleanupOnFailure(
            RegisteredEmitter registeredEmitter,
            SseEmitter.SseEventBuilder eventBuilder
    ) {
        try {
            registeredEmitter.emitter().send(eventBuilder);
        } catch (IOException | IllegalStateException ex) {
            remove(registeredEmitter.emitterId());
            registeredEmitter.emitter().complete();
        }
    }

    // subId 버킷에서 emitter를 지우고 비면 버킷 자체도 삭제한다.
    private void removeFromBucket(
            ConcurrentMap<Long, ConcurrentMap<String, SseEmitter>> index,
            Long key,
            String emitterId
    ) {
        index.computeIfPresent(key, (unused, emitters) -> {
            emitters.remove(emitterId);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    public record RegisteredEmitter(String emitterId, SseEmitter emitter) {
    }
}
