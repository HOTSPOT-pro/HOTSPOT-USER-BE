package hotspot.user.dispatch.sse.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hotspot.user.common.exception.ApplicationException;
import hotspot.user.common.exception.code.NotificationErrorCode;
import hotspot.user.common.exception.code.SubscriptionErrorCode;
import hotspot.user.dispatch.sse.controller.port.SubscribeSseService;
import hotspot.user.dispatch.sse.domain.SseConnectedPayload;
import hotspot.user.dispatch.sse.registry.SseEmitterRegistry;
import hotspot.user.subscription.domain.Subscription;
import hotspot.user.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscribeSseServiceImpl implements SubscribeSseService {

    private static final long SSE_TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final SseEmitterRegistry sseEmitterRegistry;
    private final SubscriptionService subscriptionService;

    @Override
    public SseEmitter subscribe(Long memberId, String lastEventId) {
        Long subId = resolveSubIdByMemberId(memberId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        String emitterId = sseEmitterRegistry.register(subId, emitter);

        emitter.onCompletion(() -> sseEmitterRegistry.remove(emitterId));
        emitter.onTimeout(() -> sseEmitterRegistry.remove(emitterId));
        emitter.onError(ex -> sseEmitterRegistry.remove(emitterId));

        sseEmitterRegistry.sendAndCleanupOnFailure(
                new SseEmitterRegistry.RegisteredEmitter(emitterId, emitter),
                SseEmitter.event()
                        .name("connected")
                        .id(emitterId)
                        .data(SseConnectedPayload.builder()
                                .subId(subId)
                                .lastEventId(lastEventId)
                                .connectedTime(LocalDateTime.now())
                                .build())
        );

        return emitter;
    }

    @Scheduled(fixedDelayString = "${app.notification.sse.heartbeat-interval-ms:25000}")
    public void sendHeartbeat() {
        sseEmitterRegistry.findAll()
                .forEach(registeredEmitter -> sseEmitterRegistry.sendAndCleanupOnFailure(
                        registeredEmitter,
                        SseEmitter.event()
                                .name("heartbeat")
                                .data(LocalDateTime.now())
                ));
    }

    private Long resolveSubIdByMemberId(Long memberId) {
        try {
            Subscription subscription = subscriptionService.findByMemberId(memberId);
            return subscription.getId();
        } catch (ApplicationException e) {
            if (e.getCode() == SubscriptionErrorCode.SUBSCRIPTION_NOT_FOUND) {
                throw new ApplicationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
            }
            throw e;
        }
    }
}
